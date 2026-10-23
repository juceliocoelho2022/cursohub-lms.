package br.com.cursohub.instructor.application.port.in;

import br.com.cursohub.instructor.application.model.CreateCourseCommand;
import br.com.cursohub.instructor.application.model.CreateLessonCommand;
import br.com.cursohub.instructor.domain.*;
import java.util.List;
import java.util.UUID;

public interface InstructorStudioUseCase {
    List<InstructorCourse> listCourses(UUID instructorId);
    InstructorCourseContent getCourse(UUID instructorId, UUID courseId);
    InstructorCourse createCourse(UUID instructorId, CreateCourseCommand command);
    InstructorModule addModule(UUID instructorId, UUID courseId, String title);
    InstructorLesson addLesson(UUID instructorId, UUID courseId, UUID moduleId, CreateLessonCommand command);
    InstructorCourse publishCourse(UUID instructorId, UUID courseId);
}
