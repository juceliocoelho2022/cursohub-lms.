package br.com.cursohub.learning.adapters.out.persistence;

import br.com.cursohub.learning.application.port.out.LearningContentReaderPort;
import br.com.cursohub.learning.domain.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Leitura autorizada de conteúdo: filtra matrícula pelo UUID autenticado. */
@Repository
public class LearningContentPostgresAdapter implements LearningContentReaderPort {
    private static final String ENROLLED_COURSE = """
        SELECT c.id AS course_id, c.slug, c.title, e.id AS enrollment_id
          FROM enrollments e
          JOIN courses c ON c.id = e.course_id
         WHERE e.student_id = ? AND c.slug = ?
           AND e.status IN ('ACTIVE', 'COMPLETED')
        """;

    private static final String MODULES = """
        SELECT m.id, m.title, m.position
          FROM course_modules m
         WHERE m.course_id = ?
         ORDER BY m.position
        """;

    private static final String LESSONS = """
        SELECT l.id, l.module_id, l.title, l.position, l.kind,
               l.content_url, l.body_text, l.duration_seconds,
               l.is_required, lp.id IS NOT NULL AS completed
          FROM lessons l
          JOIN course_modules m ON m.id = l.module_id
          LEFT JOIN lesson_progress lp ON lp.lesson_id = l.id AND lp.enrollment_id = ?
         WHERE m.course_id = ?
         ORDER BY m.position, l.position
        """;

    private final JdbcTemplate jdbc;

    public LearningContentPostgresAdapter(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    @Transactional(readOnly = true)
    public Optional<LearningCourse> findEnrolledCourse(UUID studentId, String slug) {
        List<CourseRow> enrolled = jdbc.query(ENROLLED_COURSE, (rs, row) -> new CourseRow(
                rs.getObject("course_id", UUID.class), rs.getObject("enrollment_id", UUID.class),
                rs.getString("slug"), rs.getString("title")), studentId, slug);

        if (enrolled.isEmpty()) return Optional.empty();
        CourseRow course = enrolled.getFirst();

        var modules = jdbc.query(MODULES, (rs, row) -> new ModuleRow(
                rs.getObject("id", UUID.class), rs.getString("title"), rs.getInt("position")
        ), course.courseId());

        // Preserve a ordem no banco e também módulos que ainda não possuem aulas.
        Map<UUID, List<Lesson>> lessonsByModule = new LinkedHashMap<>();
        for (ModuleRow module : modules) lessonsByModule.put(module.id(), new ArrayList<>());

        jdbc.query(LESSONS, rs -> {
            UUID moduleId = rs.getObject("module_id", UUID.class);
            List<Lesson> lessons = lessonsByModule.get(moduleId);
            if (lessons != null) {
                lessons.add(new Lesson(
                        rs.getObject("id", UUID.class),
                        rs.getString("title"),
                        rs.getInt("position"),
                        LessonKind.valueOf(rs.getString("kind")),
                        rs.getString("content_url"),
                        rs.getString("body_text"),
                        rs.getInt("duration_seconds"),
                        rs.getBoolean("is_required"),
                        rs.getBoolean("completed")
                ));
            }
        }, course.enrollmentId(), course.courseId());

        // Never return the body/link of any locked lesson, even if client bypasses UI.
        boolean blocked = false;
        Map<UUID,List<Lesson>> guarded = new LinkedHashMap<>();
        for (ModuleRow module : modules) {
            List<Lesson> items = new ArrayList<>();
            for (Lesson original : lessonsByModule.get(module.id())) {
                items.add(new Lesson(original.id(),original.title(),original.position(),original.kind(),
                        blocked ? null : original.contentUrl(),
                        blocked ? null : original.bodyText(),original.durationSeconds(),
                        original.required(),original.completed(),blocked));
                if((original.required() || original.kind() == LessonKind.VIDEO) && !original.completed()) blocked=true;
            }
            guarded.put(module.id(),items);
        }
        List<LearningModule> result = modules.stream()
                .map(m -> new LearningModule(m.id(), m.title(), m.position(), guarded.get(m.id())))
                .toList();
        return Optional.of(new LearningCourse(course.courseId(), course.slug(), course.title(), result));
    }

    private record CourseRow(UUID courseId, UUID enrollmentId, String slug, String title) {}
    private record ModuleRow(UUID id, String title, int position) {}
}
