# CursoHub LMS — Redes sociais e compartilhamento

**Branch:** `feature/social-links-course-sharing`. **PR #12** em rascunho, baseado em `feature/home-fiap-inspired` (PR #11). `main` preservada.

## O que foi implementado

1. Seção **04 / Comunidade & Conexões** antes do rodapé, com visual consistente com a home editorial inspirada no estilo de instituições de tecnologia.
2. Links configuráveis de YouTube, Instagram, LinkedIn, GitHub, Facebook e TikTok. Apenas canais com URL válida aparecem como links clicáveis. Os demais ficam rotulados **Em breve**.
3. Botões para copiar link da home ou de um curso e compartilhar externamente via WhatsApp, LinkedIn ou menu nativo (quando disponível).
4. **Deep link**: o URL `https://SEU_DOMINIO/?curso=java-basico-primeiros-passos-2026` abre o modal do curso correto. Se não existir ou não estiver publicado, o catálogo continua funcionando.
5. Tratamento de links locais: `localhost:3000` só funciona no próprio computador. Os botões externos ficam desativados até configurar um domínio HTTPS público, sem induzir o aluno a acreditar que a página local foi compartilhada.
6. Nenhuma conexão com contas pessoais de aluno, autenticação social, banco ou novas migrations. Não são necessários tokens nem senhas de redes sociais.

## Como adicionar suas redes

Abra `frontend/public/social-links.json` no VS Code/IntelliJ e preencha **somente os URLs verdadeiros que você controla**:

```json
{
  "siteUrl": "",
  "youtube": "",
  "instagram": "",
  "linkedin": "",
  "github": "https://github.com/juceliocoelho2022",
  "facebook": "",
  "tiktok": "",
  "whatsapp": ""
}
```

Exemplos de formato (são **exemplos, não seus endereços**):
- YouTube: `https://www.youtube.com/@SEU_CANAL`
- Instagram: `https://www.instagram.com/SEU_PERFIL/`
- LinkedIn: `https://www.linkedin.com/in/SEU_PERFIL/`
- TikTok: `https://www.tiktok.com/@SEU_PERFIL`
- Facebook: `https://www.facebook.com/SEU_PERFIL`
- WhatsApp: `https://wa.me/5511999999999` — use apenas número profissional disponibilizado publicamente.

O campo `siteUrl` é o domínio **real de produção HTTPS**, por exemplo `https://SEU-DOMINIO-REAL.com.br`. **Não coloque endereço inventado ou localhost nesse campo.** Quando ainda não tiver domínio, mantenha `""`.

O GitHub já está configurado com `https://github.com/juceliocoelho2022`. Se houver perfis separados exclusivos do CursoHub, substitua-os no arquivo.

O aplicativo valida HTTPS e o domínio da rede correspondente; links inválidos não são publicados. Nenhum dado de login ou senha deve ser colocado nesse arquivo, pois ele é público.

### Instalar a feature

```powershell
cd C:\Projetos\cursohub-lms-repo
git status
git fetch origin
git switch --track origin/feature/social-links-course-sharing

docker compose -p cursohub-lms up -d --build
docker compose -p cursohub-lms ps
curl.exe -i http://localhost:8080/actuator/health
```

Se a branch já existir localmente, use:
```powershell
git switch feature/social-links-course-sharing
git pull --ff-only origin feature/social-links-course-sharing
```

Se houver alterações locais no `git status`, preserve-as antes de mudar de branch.

Para atualizar links depois de editar o JSON:
```powershell
docker compose -p cursohub-lms up -d --build frontend
```
Depois entre em `http://localhost:3000`, pressione `Ctrl+Shift+R` e role até **Comunidade & Conexões**.

**Nunca execute `docker compose down -v`**: essa opção apaga o volume PostgreSQL e os dados existentes.

## Testes de homologação

1. GitHub abre em nova aba, com `noopener,noreferrer`.
2. Os demais canais aparecem como **Em breve** até serem configurados.
3. Após editar `social-links.json`, a rede correspondente passa a ser clicável.
4. **Copiar link** mostra sucesso após a operação terminar. Em localhost, também avisa que o link não funciona em outros computadores.
5. Na janela de um curso, **COMPARTILHAR ESTE CURSO** gera um link `?curso=slug`.
6. Com domínio publicado, abrir o link compartilhado no navegador inicia no curso correspondente.
7. Sem domínio publicado, WhatsApp e LinkedIn ficam desabilitados, para não enviar endereços locais inacessíveis.
8. Cadastro/login, matrículas, upload, avaliações, certificados e temas continuam intactos.
9. Teste responsivo em mobile e desktop.

## Limitações e próximos passos

- O compartilhamento usa URLs públicos, não mensagens enviadas pelo servidor.
- Para compartilhar com pessoas externas, hospede frontend e API num domínio público HTTPS, com rotas e segurança configuradas; atualizar apenas `siteUrl` não publica o servidor.
- Login usando Google, Facebook ou outras redes exigiria OAuth2/OIDC com verificação de identidade e é uma funcionalidade **distinta**, não implementada aqui.
- Os canais e textos são configurados por arquivo estático; um painel administrativo para editar links sem rebuild pode ser implementado depois.

## Dependências de PRs

PR #12 depende de #11 (home editorial), que depende de #10, #9 e #8. A base `main` permanece estável até integração na ordem correta e homologação visual.
