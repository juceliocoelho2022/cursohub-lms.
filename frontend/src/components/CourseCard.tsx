import type { Course } from '../services/catalog'
import { formatDuration, levels } from '../services/catalog'

interface CourseCardProps {
  course: Course
  onView: (course: Course) => void
}

const covers: Record<string, { symbol: string; theme: string; eyebrow: string }> = {
  'Programação': { symbol: '</>', theme: 'blue', eyebrow: 'DESENVOLVIMENTO' },
  'Dados': { symbol: '{ data }', theme: 'violet', eyebrow: 'DATA & ANALYTICS' },
  'Matemática': { symbol: '∑', theme: 'mint', eyebrow: 'CIÊNCIAS EXATAS' },
}

export function CourseCard({ course, onView }: CourseCardProps) {
  const cover = covers[course.category] ?? { symbol: '✦', theme: 'orange', eyebrow: course.category.toUpperCase() }

  return (
    <article className="course-card">
      <div className={`course-cover cover-${cover.theme}`}>
        <div className="cover-orbit" />
        <span className="cover-eyebrow">{cover.eyebrow}</span>
        <span className="cover-symbol" aria-hidden="true">{cover.symbol}</span>
        <span className="cover-footnote">CURSOHUB • ACADEMY</span>
      </div>
      <div className="course-content">
        <div className="course-meta">
          <span className="course-category">{course.category}</span>
          <span className="meta-dot">·</span>
          <span>{levels[course.level]}</span>
        </div>
        <h3>{course.title}</h3>
        <p>{course.description}</p>
        <div className="course-footer">
          <span className="course-duration">◷ &nbsp;{formatDuration(course.estimatedMinutes)}</span>
          <button type="button" className="course-link" onClick={() => onView(course)}
                  aria-label={`Ver detalhes de ${course.title}`}>
            Explorar curso <span aria-hidden="true">↗</span>
          </button>
        </div>
      </div>
    </article>
  )
}
