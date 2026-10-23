package br.com.cursohub.enrollment.adapters.out.persistence;

import br.com.cursohub.enrollment.application.exception.CourseUnavailableException;
import br.com.cursohub.enrollment.application.exception.EnrollmentConflictException;
import br.com.cursohub.enrollment.application.model.EnrollmentReceipt;
import br.com.cursohub.enrollment.application.port.out.EnrollmentWriterPort;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Operação transacional e idempotente: UNIQUE(student_id, course_id) impede duplicação
 * mesmo com duas requisições simultâneas. Curso deve estar PUBLICADO no instante do INSERT.
 */
@Repository
public class EnrollmentPostgresAdapter implements EnrollmentWriterPort {
    private static final String INSERT = """
        INSERT INTO enrollments (id, student_id, course_id, status)
        SELECT gen_random_uuid(), ?, c.id, 'ACTIVE'
          FROM courses c
         WHERE c.slug = ? AND c.status = 'PUBLICADO'
        ON CONFLICT (student_id, course_id) DO NOTHING
        RETURNING id, course_id, status
        """;

    private static final String FIND_EXISTING = """
        SELECT e.id, e.course_id, e.status
          FROM enrollments e
          JOIN courses c ON c.id = e.course_id
         WHERE e.student_id = ? AND c.slug = ?
        """;

    private final JdbcTemplate jdbc;

    public EnrollmentPostgresAdapter(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    @Transactional
    public EnrollmentReceipt enrollIfPublished(UUID studentId, String courseSlug) {
        List<EnrollmentReceipt> inserted = jdbc.query(INSERT,
                (rs, row) -> new EnrollmentReceipt(
                        rs.getObject("id", UUID.class), rs.getObject("course_id", UUID.class),
                        courseSlug, rs.getString("status"), true),
                studentId, courseSlug);
        if (!inserted.isEmpty()) return inserted.getFirst();

        // Curso removido, arquivado ou ainda em rascunho nunca aceita matrícula nova.
        Long published = jdbc.queryForObject("""
                SELECT count(*) FROM courses WHERE slug = ? AND status = 'PUBLICADO'
                """, Long.class, courseSlug);
        if (published == null || published == 0) throw new CourseUnavailableException();

        List<EnrollmentReceipt> previous = jdbc.query(FIND_EXISTING,
                (rs, row) -> new EnrollmentReceipt(
                        rs.getObject("id", UUID.class), rs.getObject("course_id", UUID.class),
                        courseSlug, rs.getString("status"), false),
                studentId, courseSlug);
        if (previous.isEmpty()) throw new CourseUnavailableException();
        EnrollmentReceipt current = previous.getFirst();
        if ("CANCELLED".equals(current.status())) throw new EnrollmentConflictException();
        return current;
    }
}
