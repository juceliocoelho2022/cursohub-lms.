package br.com.cursohub.instructor.adapters.out.persistence;

import br.com.cursohub.catalog.domain.CourseLevel;
import br.com.cursohub.catalog.domain.CourseStatus;
import br.com.cursohub.instructor.application.exception.*;
import br.com.cursohub.instructor.application.model.*;
import br.com.cursohub.instructor.application.port.out.InstructorStudioPort;
import br.com.cursohub.instructor.domain.*;
import br.com.cursohub.learning.domain.LessonKind;
import java.util.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador SQL do Estúdio. Toda consulta usa instructor_id do JWT,
 * e operações de escrita bloqueiam a linha do curso para serializar posições.
 */
@Repository
public class InstructorPostgresAdapter implements InstructorStudioPort {
    private static final String SELECT_COURSE = """
        SELECT id, slug, title, description, category, level, status, estimated_minutes
          FROM courses WHERE id = ? AND instructor_id = ?
        """;

    private final JdbcTemplate jdbc;

    public InstructorPostgresAdapter(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    @Transactional(readOnly = true)
    public List<InstructorCourse> listOwned(UUID instructorId) {
        return jdbc.query("""
            SELECT id, slug, title, description, category, level, status, estimated_minutes
              FROM courses
             WHERE instructor_id = ?
             ORDER BY created_at DESC, id DESC
            """, (rs, i) -> mapCourse(rs), instructorId);
    }

    @Override
    @Transactional(readOnly = true)
    public InstructorCourseContent findOwned(UUID instructorId, UUID courseId) {
        InstructorCourse course = owned(instructorId, courseId);
        var rows = jdbc.query("""
            SELECT id, title, position
              FROM course_modules WHERE course_id = ? ORDER BY position
            """, (rs, i) -> new ModuleRow(rs.getObject("id", UUID.class),
                    rs.getString("title"), rs.getInt("position")), courseId);

        Map<UUID, List<InstructorLesson>> lessonsByModule = new LinkedHashMap<>();
        for (var row : rows) lessonsByModule.put(row.id(), new ArrayList<>());

        jdbc.query("""
            SELECT l.id, l.module_id, l.title, l.position, l.kind, l.content_url,
                   l.body_text, l.duration_seconds, l.is_required
              FROM lessons l JOIN course_modules m ON m.id = l.module_id
             WHERE m.course_id = ?
             ORDER BY m.position, l.position
            """, rs -> {
                var module = lessonsByModule.get(rs.getObject("module_id", UUID.class));
                if (module != null) module.add(new InstructorLesson(
                        rs.getObject("id", UUID.class), rs.getObject("module_id", UUID.class),
                        rs.getString("title"), rs.getInt("position"),
                        LessonKind.valueOf(rs.getString("kind")),
                        rs.getString("content_url"), rs.getString("body_text"),
                        rs.getInt("duration_seconds"), rs.getBoolean("is_required")));
            }, courseId);

        List<InstructorModule> modules = rows.stream()
                .map(row -> new InstructorModule(row.id(), row.title(), row.position(),
                        lessonsByModule.get(row.id())))
                .toList();
        return new InstructorCourseContent(course, modules);
    }

    @Override
    @Transactional
    public InstructorCourse createDraft(UUID instructorId, CreateCourseCommand command) {
        UUID id = UUID.randomUUID();
        try {
            jdbc.update("""
                INSERT INTO courses (id, instructor_id, slug, title, description,
                                     category, level, status, estimated_minutes)
                VALUES (?, ?, ?, ?, ?, ?, ?, 'RASCUNHO', ?)
                """, id, instructorId, command.slug(), command.title(), command.description(),
                command.category(), command.level().name(), command.estimatedMinutes());
        } catch (DuplicateKeyException ex) {
            throw new InstructorContentConflictException("O slug informado já está em uso");
        }
        return owned(instructorId, id);
    }

    @Override
    @Transactional
    public InstructorModule appendModule(UUID instructorId, UUID courseId, String title) {
        assertDraftLocked(instructorId, courseId);
        Integer position = jdbc.queryForObject("""
                SELECT COALESCE(MAX(position), 0) + 1 FROM course_modules WHERE course_id = ?
                """, Integer.class, courseId);
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO course_modules(id, course_id, title, position) VALUES (?, ?, ?, ?)",
                id, courseId, title, Objects.requireNonNull(position));
        return new InstructorModule(id, title, position, List.of());
    }

    @Override
    @Transactional
    public InstructorLesson appendLesson(UUID instructorId, UUID courseId, UUID moduleId,
                                         CreateLessonCommand command) {
        assertDraftLocked(instructorId, courseId);
        Long moduleCount = jdbc.queryForObject("""
                SELECT COUNT(*) FROM course_modules WHERE id = ? AND course_id = ?
                """, Long.class, moduleId, courseId);
        if (moduleCount == null || moduleCount == 0) throw new InstructorCourseNotFoundException();
        Integer position = jdbc.queryForObject("""
                SELECT COALESCE(MAX(position), 0) + 1 FROM lessons WHERE module_id = ?
                """, Integer.class, moduleId);
        UUID id = UUID.randomUUID();
        jdbc.update("""
            INSERT INTO lessons(id, module_id, title, position, kind, content_url,
                                body_text, duration_seconds, is_required)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, id, moduleId, command.title(), Objects.requireNonNull(position),
             command.kind().name(), command.contentUrl(), command.bodyText(),
             command.durationSeconds(), command.required());
        return new InstructorLesson(id, moduleId, command.title(), position,
                command.kind(), command.contentUrl(), command.bodyText(),
                command.durationSeconds(), command.required());
    }

    @Override
    @Transactional
    public InstructorCourse publish(UUID instructorId, UUID courseId) {
        CourseStatus status = lockOwned(instructorId, courseId);
        if (status == CourseStatus.PUBLICADO) return owned(instructorId, courseId);
        if (status != CourseStatus.RASCUNHO)
            throw new InstructorContentConflictException("Curso arquivado não pode ser publicado");
        Long required = jdbc.queryForObject("""
            SELECT COUNT(*)
              FROM course_modules m JOIN lessons l ON l.module_id = m.id
             WHERE m.course_id = ? AND l.is_required = TRUE
            """, Long.class, courseId);
        if (required == null || required < 1)
            throw new InstructorContentConflictException("Adicione ao menos uma aula obrigatória antes de publicar");
        Long videoMissing=jdbc.queryForObject("""
            SELECT COUNT(*) FROM course_modules m JOIN lessons l ON l.module_id=m.id
            LEFT JOIN course_assets a ON a.lesson_id=l.id
            WHERE m.course_id=? AND l.kind='VIDEO' AND a.id IS NULL
            """,Long.class,courseId);
        if(videoMissing!=null && videoMissing>0)
            throw new InstructorContentConflictException("Envie o MP4/WEBM de todas as videoaulas antes de publicar");
        jdbc.update("""
            UPDATE courses
               SET status = 'PUBLICADO', published_at = now(), updated_at = now()
             WHERE id = ? AND instructor_id = ? AND status = 'RASCUNHO'
            """, courseId, instructorId);
        return owned(instructorId, courseId);
    }

    private CourseStatus lockOwned(UUID instructorId, UUID courseId) {
        var statuses = jdbc.query("""
            SELECT status FROM courses WHERE id = ? AND instructor_id = ? FOR UPDATE
            """, (rs, row) -> CourseStatus.valueOf(rs.getString("status")), courseId, instructorId);
        if (statuses.isEmpty()) throw new InstructorCourseNotFoundException();
        return statuses.getFirst();
    }

    private void assertDraftLocked(UUID instructorId, UUID courseId) {
        if (lockOwned(instructorId, courseId) != CourseStatus.RASCUNHO)
            throw new InstructorContentConflictException("Edite módulos e aulas apenas de cursos em rascunho");
    }

    private InstructorCourse owned(UUID instructorId, UUID courseId) {
        var rows = jdbc.query(SELECT_COURSE, (rs, i) -> mapCourse(rs), courseId, instructorId);
        if (rows.isEmpty()) throw new InstructorCourseNotFoundException();
        return rows.getFirst();
    }

    private static InstructorCourse mapCourse(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new InstructorCourse(
                rs.getObject("id", UUID.class), rs.getString("slug"),
                rs.getString("title"), rs.getString("description"),
                rs.getString("category"), CourseLevel.valueOf(rs.getString("level")),
                CourseStatus.valueOf(rs.getString("status")), rs.getInt("estimated_minutes"));
    }

    private record ModuleRow(UUID id, String title, int position) {}
}
