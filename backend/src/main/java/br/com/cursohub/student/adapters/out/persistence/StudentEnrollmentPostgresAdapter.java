package br.com.cursohub.student.adapters.out.persistence;

import br.com.cursohub.student.application.port.out.StudentEnrollmentReaderPort;
import br.com.cursohub.student.domain.EnrollmentStatus;
import br.com.cursohub.student.domain.StudentEnrollment;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador SQL de leitura. Contagens são derivadas das aulas obrigatórias,
 * sem confiar em dados de progresso enviados pelo navegador.
 */
@Repository
public class StudentEnrollmentPostgresAdapter implements StudentEnrollmentReaderPort {
    private static final String QUERY = """
        SELECT e.id AS enrollment_id,
               c.id AS course_id,
               c.slug AS course_slug,
               c.title AS course_title,
               c.category AS category,
               e.status AS enrollment_status,
               e.enrolled_at AS enrolled_at,
               (SELECT COUNT(*)
                  FROM course_modules m
                  JOIN lessons l ON l.module_id = m.id
                 WHERE m.course_id = c.id AND l.is_required = TRUE) AS required_lessons,
               (SELECT COUNT(*)
                  FROM lesson_progress lp
                  JOIN lessons l ON l.id = lp.lesson_id AND l.is_required = TRUE
                  JOIN course_modules m ON m.id = l.module_id
                 WHERE lp.enrollment_id = e.id AND m.course_id = c.id) AS completed_lessons
          FROM enrollments e
          JOIN courses c ON c.id = e.course_id
         WHERE e.student_id = ?
           AND e.status IN ('ACTIVE', 'COMPLETED')
         ORDER BY e.enrolled_at DESC, e.id DESC
        """;

    private final JdbcTemplate jdbc;

    public StudentEnrollmentPostgresAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentEnrollment> findActiveAndCompletedByStudent(UUID studentId) {
        return jdbc.query(QUERY, (rs, rowNum) -> new StudentEnrollment(
                rs.getObject("enrollment_id", UUID.class),
                rs.getObject("course_id", UUID.class),
                rs.getString("course_slug"),
                rs.getString("course_title"),
                rs.getString("category"),
                EnrollmentStatus.valueOf(rs.getString("enrollment_status")),
                rs.getInt("required_lessons"),
                rs.getInt("completed_lessons"),
                rs.getTimestamp("enrolled_at").toInstant()
        ), studentId);
    }
}
