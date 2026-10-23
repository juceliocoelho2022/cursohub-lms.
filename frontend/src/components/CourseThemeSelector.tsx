import type { CourseTheme } from '../services/courseTheme'

interface Props {
  value: CourseTheme
  onChange: (theme: CourseTheme) => void
  compact?: boolean
}

const options: ReadonlyArray<{ value: CourseTheme; label: string; icon: string; description: string }> = [
  { value: 'light', label: 'Claro', icon: '☀', description: 'Fundo claro, contraste tradicional' },
  { value: 'moderate', label: 'Moderado', icon: '◐', description: 'Cores suaves para conforto visual' },
  { value: 'black', label: 'Black', icon: '☾', description: 'Fundo escuro para leitura noturna' },
]

/** Controle acessível de aparência; não exige login nem grava dados pessoais. */
export function CourseThemeSelector({ value, onChange, compact = false }: Props) {
  return (
    <div className={`course-theme-selector ${compact ? 'course-theme-compact' : ''}`}>
      <span className="course-theme-label" id={compact ? 'course-theme-modal-label' : undefined}>
        <span aria-hidden="true">◑</span> Aparência
      </span>
      <div className="course-theme-options" role="group" aria-label="Escolha o tema de visualização dos cursos">
        {options.map(option => (
          <button
            key={option.value}
            type="button"
            className={`course-theme-option ${value === option.value ? 'is-selected' : ''}`}
            onClick={() => onChange(option.value)}
            aria-pressed={value === option.value}
            title={option.description}
          >
            <span className="course-theme-option-icon" aria-hidden="true">{option.icon}</span>
            {option.label}
          </button>
        ))}
      </div>
    </div>
  )
}
