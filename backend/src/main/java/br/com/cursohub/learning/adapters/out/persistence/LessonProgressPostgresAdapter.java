package br.com.cursohub.learning.adapters.out.persistence;

import br.com.cursohub.learning.application.model.LessonCompletionReceipt;
import br.com.cursohub.learning.application.port.out.LessonProgressWriterPort;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Marca como concluída somente uma aula do curso em que o aluno está matriculado.
 * A constraint UNIQUE(enrollment_id, lesson_id) mantém a operação idempotente.
 */
@Repository
public class LessonProgressPostgresAdapter implements LessonProgressWriterPort {
    private static final String INSERT_PROGRESS = """
        INSERT INTO lesson_progress (enrollment_id, lesson_id)
        SELECT e.id, l.id
          FROM enrollments e
          JOIN courses c ON c.id = e.course_id
          JOIN course_modules m ON m.course_id = c.id
          JOIN lessons l ON l.module_id = m.id
         WHERE e.student_id = ? AND c.slug = ? AND l.id = ?
           AND e.status = 'ACTIVE'
        ON CONFLICT (enrollment_id, lesson_id) DO NOTHING
        RETURNING lesson_id
        """;

    private static final String EXISTING_PROGRESS = """
        SELECT lp.lesson_id
          FROM enrollments e
          JOIN courses c ON c.id = e.course_id
          JOIN course_modules m ON m.course_id = c.id
          JOIN lessons l ON l.module_id = m.id
          JOIN lesson_progress lp ON lp.enrollment_id = e.id AND lp.lesson_id = l.id
         WHERE e.student_id = ? AND c.slug = ? AND l.id = ?
           AND e.status IN ('ACTIVE', 'COMPLETED')
        """;

    private final JdbcTemplate jdbc;

    public LessonProgressPostgresAdapter(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    @Transactional
    public Optional<LessonCompletionReceipt> markCompleted(UUID studentId, String slug, UUID lessonId) {
        List<UUID> inserted = jdbc.query(INSERT_PROGRESS,
                (rs, row) -> rs.getObject("lesson_id", UUID.class), studentId, slug, lessonId);
        if (!inserted.isEmpty()) return Optional.of(new LessonCompletionReceipt(lessonId, true));

        // Resposta idempotente para requisições repetidas. Aluno, curso e aula
        // precisam continuar associados, inclusive em uma matrícula COMPLETED.
        List<UUID> existing = jdbc.query(EXISTING_PROGRESS,
                (rs, row) -> rs.getObject("lesson_id", UUID.class), studentId, slug, lessonId);
        if (!existing.isEmpty()) return Optional.of(new LessonCompletionReceipt(lessonId, false));

        return Optional.empty();
    }
}
