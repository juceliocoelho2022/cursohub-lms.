# Primeiro instrutor - ambiente local

O catalogo de demonstracao cria `demo-instrutor@cursohub.invalid` como **inativo** com senha inutilizavel. Nao reative esse registro nem altere a senha diretamente no banco.

## Procedimento

1. Faca backup do volume PostgreSQL antes de provisionar uma conta real.
2. No arquivo `.env` **local** (nunca commitar), configure temporariamente:

```dotenv
BOOTSTRAP_INSTRUCTOR_ENABLED=true
BOOTSTRAP_INSTRUCTOR_NAME=Instrutor CursoHub
BOOTSTRAP_INSTRUCTOR_EMAIL=seu-email-de-instrutor@example.com
BOOTSTRAP_INSTRUCTOR_PASSWORD=gere-uma-senha-exclusiva-de-no-minimo-12-caracteres
```

3. Execute `docker compose up -d --build backend`. Confirme nos logs **apenas** a mensagem de bootstrap (sem exibir os valores das variaveis).
4. Remova as quatro variaveis do `.env` ou defina `BOOTSTRAP_INSTRUCTOR_ENABLED=false`; recrie o backend com `docker compose up -d --force-recreate backend`.
5. Entre com as credenciais escolhidas no CursoHub; altere a senha caso tenha sido exposta no terminal ou em capturas.

## Seguranca e regras

- O bootstrap somente executa quando `BOOTSTRAP_INSTRUCTOR_ENABLED=true`.
- Exige nome, email valido, senha entre 12 e 72 bytes e email ainda inexistente.
- Recusa operacao caso ja exista **algum instrutor ativo**; nunca sobrescreve usuario ou senha.
- Cria uma conta `INSTRUCTOR` ativa com hash BCrypt pelo adaptador existente.
- O login publico continua criando somente `STUDENT`.
- Nao ha endpoint HTTP de provisionamento.
- O bootstrap e intencionalmente uma tarefa de inicializacao unica. **Desative-o imediatamente** apos a primeira execucao. Se permanecer ativado, a proxima inicializacao recusara o boot.
- Nao use este fluxo como sistema de gestao de multiplos instrutores; essa funcionalidade requer operacao administrativa autenticada e auditada.
