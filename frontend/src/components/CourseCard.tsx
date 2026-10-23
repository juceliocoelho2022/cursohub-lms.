import type { Course } from '../services/catalog'
import { formatDuration, levels, money } from '../services/catalog'
import { publicCoverUrl } from '../services/courseCover'

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
  const coverImage = publicCoverUrl(course.thumbnailUrl)

  return (
    <article className="course-card">
      <div className={`course-cover cover-${cover.theme} ${coverImage?'course-cover-photo':''}`}>
        {coverImage
          ? <img src={coverImage} loading="lazy" alt={`Capa do curso ${course.title}`}/>
          : <>
              <div className="cover-orbit" />
              <span className="cover-eyebrow">{cover.eyebrow}</span>
              <span className="cover-symbol" aria-hidden="true">{cover.symbol}</span>
              <span className="cover-footnote">CURSOHUB • ACADEMY</span>
            </>}
      </div>
      <div className="course-content">
        <span className="course-card-learning-tag">CURSO ONLINE · {levels[course.level].toUpperCase()}</span>
        <div className="course-meta">
          <span className="course-category">{course.category}</span>
          <span className="meta-dot">·</span>
          <span>{levels[course.level]}</span>
        </div>
        <h3>{course.title}</h3>
        <div className={`course-price ${course.priceCents === 0 ? 'course-price-free' : ''}`}>
          {course.priceCents===0?'Grátis':money(course.priceCents)}
          {course.priceCents>0 && <small>Venda em preparação</small>}
        </div>
        <p className="course-card-description">{course.description}</p>
        <div className="course-footer">
          <span className="course-duration">◷ &nbsp;{formatDuration(course.estimatedMinutes)}</span>
          <button type="button" className="course-link" onClick={() => onView(course)}
                  aria-label={`Ver detalhes de ${course.title}`}>
            Ver detalhes <span aria-hidden="true">↗</span>
          </button>
        </div>
      </div>
    </article>
  )
}
