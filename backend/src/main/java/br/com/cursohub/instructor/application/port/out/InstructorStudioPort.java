package br.com.cursohub.instructor.application.port.out;

import br.com.cursohub.instructor.application.model.CreateCourseCommand;
import br.com.cursohub.instructor.application.model.CreateLessonCommand;
import br.com.cursohub.instructor.domain.*;
import java.util.List;
import java.util.UUID;

/** Persistência contratual. Nenhuma referência a JPA/JdbcTemplate no núcleo. */
public interface InstructorStudioPort {
    List<InstructorCourse> listOwned(UUID instructorId);
    InstructorCourseContent findOwned(UUID instructorId, UUID courseId);
    InstructorCourse createDraft(UUID instructorId, CreateCourseCommand command);
    InstructorModule appendModule(UUID instructorId, UUID courseId, String title);
    InstructorLesson appendLesson(UUID instructorId, UUID courseId, UUID moduleId, CreateLessonCommand command);
    InstructorCourse publish(UUID instructorId, UUID courseId);
}
