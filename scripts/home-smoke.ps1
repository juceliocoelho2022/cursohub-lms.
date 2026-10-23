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
        Write-Host "[OK] $Name - HTTP 200" -ForegroundColor Green
    } catch {
        $script:failures++
        Write-Host "[FALHA] $Name - $($_.Exception.Message)" -ForegroundColor Red
    }
}

$BaseUrl = $BaseUrl.TrimEnd("/")
$BackendUrl = $BackendUrl.TrimEnd("/")

Test-Endpoint "Pagina inicial" $BaseUrl {
    param($response)
    if ($response.Content -notmatch 'id=["'']root["'']') {
        throw "HTML inicial inesperado: root React nao encontrado."
    }
}

Test-Endpoint "Saude da API" "$BackendUrl/actuator/health" {
    param($response)
    $health = ConvertFrom-Json -InputObject ([string]$response.Content)
    $actualStatus = [string]$health.status
    if ($actualStatus.Trim() -ne 'UP') {
        throw ("Healthcheck inesperado: status='{0}', resposta='{1}'" -f $actualStatus, [string]$response.Content)
    }
}

Test-Endpoint "Catalogo pelo Nginx" "$BaseUrl/api/v1/courses?page=0&size=6" {
    param($response)
    $body = $response.Content | ConvertFrom-Json
    if ($null -eq $body.content -or $null -eq $body.totalElements) {
        throw "Resposta do catalogo sem os campos esperados."
    }
}

Test-Endpoint "Busca pelo Nginx" "$BaseUrl/api/v1/courses?q=Java&page=0&size=6" {
    param($response)
    $body = $response.Content | ConvertFrom-Json
    if ($null -eq $body.content) { throw "Resposta da busca invalida." }
}

if ($failures -gt 0) {
    Write-Host "Smoke test: $failures verificacao(oes) com falha." -ForegroundColor Red
    exit 1
}
Write-Host "Smoke test basico aprovado. Ainda e necessario validar visual, autenticacao e matricula manualmente." -ForegroundColor Green
