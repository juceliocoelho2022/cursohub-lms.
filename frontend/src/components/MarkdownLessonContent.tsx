import ReactMarkdown from 'react-markdown'
import remarkGfm from 'remark-gfm'
import rehypeHighlight from 'rehype-highlight'
import rehypeSanitize, { defaultSchema } from 'rehype-sanitize'
import { safeMaterialUrl } from '../services/learning'

// HTML escrito pelo instrutor é ignorado, e apenas classes de realce
// produzidas pelo highlighter são preservadas após a sanitização.
const markdownSchema = {
  ...defaultSchema,
  attributes: {
    ...defaultSchema.attributes,
    code: [...(defaultSchema.attributes?.code ?? []), ['className', /^language-/]],
    span: [...(defaultSchema.attributes?.span ?? []), ['className', /^hljs-/]],
  },
}

interface Props {
  content: string
}

export function MarkdownLessonContent({ content }: Props) {
  return (
    <div className="learning-markdown">
      <ReactMarkdown
        remarkPlugins={[remarkGfm]}
        rehypePlugins={[[rehypeHighlight, { detect: false }], [rehypeSanitize, markdownSchema]]}
        skipHtml
        disallowedElements={['img']}
        components={{
          a({ href, children }) {
            // Material do professor: não navegar para javascript:, data:, http: etc.
            const url = safeMaterialUrl(href ?? null)
            return url ? (
              <a href={url} target="_blank" rel="noopener noreferrer">{children}</a>
            ) : (
              <span>{children}</span>
            )
          },
        }}
      >
        {content}
      </ReactMarkdown>
    </div>
  )
}
