# Homologação da Home v1.0 — CursoHub

Este documento registra os critérios de aceite da página inicial. O smoke test verifica disponibilidade e contratos mínimos; **não substitui testes de interface nem garante a experiência de matrícula**.

## Pré-requisitos
- Docker Compose com `db`, `backend` e `frontend` em execução.
- Preservar os volumes PostgreSQL existentes. Não executar `docker compose down -v`.
- Confirmar qual projeto Compose e qual banco contêm os dados que devem ser validados.

## Smoke test no Windows (PowerShell)

```powershell
docker compose ps
powershell -ExecutionPolicy Bypass -File .\scripts\home-smoke.ps1
```

Se a API estiver exposta na porta 8081:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\home-smoke.ps1 -BackendUrl http://localhost:8081
```

O teste valida a Home, o healthcheck da API, o catálogo e a busca via proxy Nginx. Um catálogo vazio pode ser tecnicamente válido; não prova que existam cursos publicados.

## Checklist de homologação manual

- [ ] Abrir a Home no desktop e no celular; verificar layout em 320px, 768px e 1280px.
- [ ] Confirmar campo de busca no Hero; pesquisar `Java` e validar resultado no catálogo.
- [ ] Filtrar por categoria e nível; limpar filtros; validar lista vazia e mensagens de erro.
- [ ] Confirmar capas reais e fallback para curso sem imagem.
- [ ] Abrir detalhes de um curso gratuito e verificar nível, duração e descrição.
- [ ] Sem autenticação: CTA solicita login ou cadastro.
- [ ] Com conta STUDENT: matrícula gratuita concluída e curso disponível no painel.
- [ ] Com curso pago: CTA de pagamento/matrícula não aparece como disponível antes do checkout.
- [ ] Navegar somente por teclado e verificar foco, rótulos e funcionamento dos botões.
- [ ] Verificar DevTools Console e Network sem erros inesperados.
- [ ] Confirmar que redes sociais ainda não cadastradas não exibem links enganosos.

## Critério de aceite

Home v1.0 fica **homologada** somente após build/CI verde, smoke test aprovado, testes manuais relevantes executados e revisão de conteúdo. Não marcar como pronta apenas porque os containers estão `Up`.
