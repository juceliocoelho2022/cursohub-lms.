param(
    [string]$BaseUrl = "http://localhost:3000",
    [string]$BackendUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"
$failures = 0

function Test-Endpoint {
    param([string]$Name, [string]$Url, [scriptblock]$Validate)
    try {
        $response = Invoke-WebRequest -Uri $Url -TimeoutSec 15 -UseBasicParsing
        if ($response.StatusCode -ne 200) {
            throw "HTTP $($response.StatusCode)"
        }
        if ($Validate) { & $Validate $response }
        Write-Host "[OK] $Name — HTTP 200" -ForegroundColor Green
    } catch {
        $script:failures++
        Write-Host "[FALHA] $Name — $($_.Exception.Message)" -ForegroundColor Red
    }
}

$BaseUrl = $BaseUrl.TrimEnd("/")
$BackendUrl = $BackendUrl.TrimEnd("/")

Test-Endpoint "Página inicial" $BaseUrl {
    param($response)
    if ($response.Content -notmatch '<div id="root"></div>') {
        throw "HTML inicial inesperado: root React não encontrado."
    }
}

Test-Endpoint "Saúde da API" "$BackendUrl/actuator/health" {
    param($response)
    $body = $response.Content | ConvertFrom-Json
    if ($body.status -ne "UP") { throw "API não está UP." }
}

Test-Endpoint "Catálogo pelo Nginx" "$BaseUrl/api/v1/courses?page=0&size=6" {
    param($response)
    $body = $response.Content | ConvertFrom-Json
    if ($null -eq $body.content -or $null -eq $body.totalElements) {
        throw "Resposta do catálogo sem os campos esperados."
    }
}

Test-Endpoint "Busca pelo Nginx" "$BaseUrl/api/v1/courses?q=Java&page=0&size=6" {
    param($response)
    $body = $response.Content | ConvertFrom-Json
    if ($null -eq $body.content) { throw "Resposta da busca inválida." }
}

if ($failures -gt 0) {
    Write-Host "Smoke test: $failures verificação(ões) com falha." -ForegroundColor Red
    exit 1
}
Write-Host "Smoke test básico aprovado. Ainda é necessário validar visual, autenticação e matrícula manualmente." -ForegroundColor Green
