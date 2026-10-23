package br.com.cursohub.instructor.application;

import static org.junit.jupiter.api.Assertions.*;

import br.com.cursohub.catalog.domain.CourseLevel;
import br.com.cursohub.catalog.domain.CourseStatus;
import br.com.cursohub.instructor.application.exception.InvalidInstructorContentException;
import br.com.cursohub.instructor.application.model.*;
import br.com.cursohub.instructor.application.port.out.InstructorStudioPort;
import br.com.cursohub.instructor.domain.*;
import br.com.cursohub.learning.domain.LessonKind;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InstructorStudioServiceTest {
    private final UUID instructor = UUID.randomUUID();
    private final UUID courseId = UUID.randomUUID();
    private final UUID moduleId = UUID.randomUUID();

    @Test
    void criaCursoEmRascunhoComInstrutorAutenticado() {
        var fake = new FakeStore();
        var service = new InstructorStudioService(fake);
        var draft = service.createCourse(instructor, new CreateCourseCommand(
                "java-moderno", "Java Moderno", "Descrição de um curso completo",
                "Programação", CourseLevel.INICIANTE, 300));
        assertEquals(CourseStatus.RASCUNHO, draft.status());
        assertEquals(instructor, fake.receivedInstructor);
        assertEquals("java-moderno", fake.receivedCommand.slug());
    }

    @Test
    void rejeitaSlugInvalidoEConteudoInsuficienteAntesDePersistir() {
        var fake = new FakeStore();
        var service = new InstructorStudioService(fake);
        assertThrows(InvalidInstructorContentException.class, () -> service.createCourse(instructor,
                new CreateCourseCommand("Java Curso", "Curso Java", "Descrição do curso",
                        "Programação", CourseLevel.INICIANTE, 10)));
        assertThrows(InvalidInstructorContentException.class, () -> service.createCourse(instructor,
                new CreateCourseCommand("curso-java", "A", "Descrição do curso",
                        "Programação", CourseLevel.INICIANTE, 10)));
        assertNull(fake.receivedCommand);
    }

    @Test
    void validaTextosDeAulaSemConfiarNoFrontend() {
        var service = new InstructorStudioService(new FakeStore());
        assertThrows(InvalidInstructorContentException.class, () -> service.addLesson(
                instructor, courseId, moduleId, new CreateLessonCommand(
                        "Aula de leitura", LessonKind.TEXT, null, "oi", 60, true)));
    }

    @Test
    void proibeJavascriptHttpEUrlSemHostEmVideoOuPdf() {
        var service = new InstructorStudioService(new FakeStore());
        for (String url : List.of("javascript:alert(1)", "http://example.com/video.mp4", "https:///sem-host")) {
            assertThrows(InvalidInstructorContentException.class, () -> service.addLesson(
                    instructor, courseId, moduleId, new CreateLessonCommand(
                            "Vídeo introdutório", LessonKind.VIDEO, url, null, 60, true)));
        }
    }

    @Test
    void aceitaConteudoHttpsEValidaTitulosDeModulo() {
        var store = new FakeStore();
        var service = new InstructorStudioService(store);
        var result = service.addLesson(instructor, courseId, moduleId, new CreateLessonCommand(
                "Vídeo introdutório", LessonKind.VIDEO,
                "https://example.com/video.mp4", "", 60, true));
        assertEquals(LessonKind.VIDEO, result.kind());
        assertEquals(instructor, store.receivedInstructor);
        assertThrows(InvalidInstructorContentException.class,
                () -> service.addModule(instructor, courseId, "A"));
    }

    private final class FakeStore implements InstructorStudioPort {
        UUID receivedInstructor;
        CreateCourseCommand receivedCommand;

        @Override public List<InstructorCourse> listOwned(UUID id) { return List.of(); }
        @Override public InstructorCourseContent findOwned(UUID id, UUID courseId) {
            return new InstructorCourseContent(course(), List.of());
        }
        @Override public InstructorCourse createDraft(UUID id, CreateCourseCommand cmd) {
            receivedInstructor = id;
            receivedCommand = cmd;
            return course();
        }
        @Override public InstructorModule appendModule(UUID id, UUID courseId, String title) {
            receivedInstructor = id;
            return new InstructorModule(moduleId, title, 1, List.of());
        }
        @Override public InstructorLesson appendLesson(UUID id, UUID courseId, UUID moduleId, CreateLessonCommand cmd) {
            receivedInstructor = id;
            return new InstructorLesson(UUID.randomUUID(), moduleId, cmd.title(), 1, cmd.kind(),
                    cmd.contentUrl(), cmd.bodyText(), cmd.durationSeconds(), cmd.required());
        }
        @Override public InstructorCourse publish(UUID id, UUID courseId) { return course(); }

        private InstructorCourse course() {
            return new InstructorCourse(courseId, "java-moderno", "Java Moderno",
                    "Descrição de um curso completo", "Programação",
                    CourseLevel.INICIANTE, CourseStatus.RASCUNHO, 300);
        }
    }
}
