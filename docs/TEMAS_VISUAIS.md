# CursoHub — Temas de visualização do aluno

**Implementação:** `feature/course-enrollment` (pendente de revisão e merge).
**Escopo:** frontend React; não precisa de alteração do banco de dados.

## Funcionalidade

O aluno pode escolher entre três modos de aparência na área de cursos:

| Modo | Estilo | Cenário |
|---|---|---|
| **Claro** | Fundo claro, texto escuro; tema original do CursoHub | Leitura em ambientes iluminados |
| **Moderado** | Fundo cinza suave, contraste equilibrado, superfícies discretas | Conforto visual / uso prolongado |
| **Black** | Fundo quase preto, texto claro e realces em verde | Preferência pessoal por modo escuro |

Os botões estão presentes no **Dashboard do Aluno**, no **Catálogo de Cursos** e na **janela de detalhes do curso**. A troca é imediata, sem recarregar a página. A área pública também pode utilizar esses temas, e o cabeçalho preserva a identidade visual.

## Componentes e método principal

- `frontend/src/components/CourseThemeSelector.tsx`: controle com opções acessíveis por teclado, atributo `aria-pressed` e rótulos em português.
- `frontend/src/services/courseTheme.ts`: `loadCourseTheme()`, `saveCourseTheme()` e `isCourseTheme()`.
- `frontend/src/App.tsx`: estado compartilhado entre catálogo, dashboard e modal, com `data-course-theme` no container principal.
- `frontend/src/styles.css`: variáveis de cor CSS por tema, com propagação para cartões, filtros, painéis, tipografia, formulários e detalhes do curso.

Exemplo simplificado:

```tsx
const [theme, setTheme] = useState<CourseTheme>(loadCourseTheme)

useEffect(() => saveCourseTheme(theme), [theme])

return (
  <div className="app-shell" data-course-theme={theme}>
    <CourseThemeSelector value={theme} onChange={setTheme} />
  </div>
)
```

## Persistência e privacidade

A escolha é gravada apenas em `localStorage` na chave `cursohub.course.theme`. Guardamos somente uma string: `light`, `moderate` ou `black`; **nunca o JWT ou a senha**.

Se o armazenamento estiver indisponível, a escolha funciona enquanto a página estiver aberta e o aplicativo usa **Claro** como padrão na próxima visita.

**Limitação:** a preferência é por navegador/dispositivo, **não por conta** e não sincroniza entre dispositivos. Sincronização do perfil do aluno via backend poderá ser implementada futuramente, se houver requisito.

## Critérios de aceite

1. Selecionar **Moderado** muda cores no catálogo, dashboard e detalhes do curso.
2. Selecionar **Black** apresenta fundo escuro e texto legível em cartões, inputs, menu de matrícula e progresso.
3. Selecionar **Claro** recupera a aparência tradicional.
4. Abrir detalhes do curso reflete o tema já escolhido; alterar ali mantém o mesmo tema ao voltar ao dashboard.
5. Recarregar a aba mantém a seleção, quando `localStorage` está disponível.
6. Alternar o tema não altera a sessão JWT nem as matrículas.
7. Os três botões são acessíveis por teclado, com foco visível e informação de qual está selecionado.
8. Respeita a preferência do sistema por movimento reduzido, sem transições de cor nesse caso.

## Validação local

```powershell
cd C:\Projetos\cursohub-lms-repo
git fetch origin
git switch --track origin/feature/course-enrollment
docker compose -p cursohub-lms up -d --build
```

Na interface em `http://localhost:3000`: entre com uma conta de aluno, clique em **Meu painel**, altere o tema e depois escolha **Explorar cursos**. Ao abrir detalhes de qualquer curso, o tema escolhido deve ser respeitado.

**Importante:** preserve o `.env` e o volume de dados; não use `docker compose down -v`.
