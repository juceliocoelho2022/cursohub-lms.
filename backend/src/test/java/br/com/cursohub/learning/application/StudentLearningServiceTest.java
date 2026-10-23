package br.com.cursohub.learning.application;

import static org.junit.jupiter.api.Assertions.*;

import br.com.cursohub.learning.application.exception.*;
import br.com.cursohub.learning.application.model.LessonCompletionReceipt;
import br.com.cursohub.learning.application.port.out.*;
import br.com.cursohub.learning.domain.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class StudentLearningServiceTest {
    private final UUID student = UUID.randomUUID();
    private final UUID lessonId = UUID.randomUUID();
    private final UUID courseId = UUID.randomUUID();

    @Test
    void mostraConteudoEProgressoRealDasAulasObrigatorias() {
        LearningCourse course = new LearningCourse(courseId, "java-basico", "Java Básico", List.of(
            new LearningModule(UUID.randomUUID(), "Introdução", 1, List.of(
                new Lesson(lessonId, "Primeira aula", 1, LessonKind.TEXT, null,
                           "Olá Java", 120, true, true),
                new Lesson(UUID.randomUUID(), "Segunda aula", 2, LessonKind.PDF,
                           "https://example.com/apostila.pdf", null, 180, true, false),
                new Lesson(UUID.randomUUID(), "Material extra", 3, LessonKind.VIDEO,
                           "https://example.com/video.mp4", null, 360, false, false)
            ))
        ));
        StudentLearningService service = new StudentLearningService(
                (user,slug)->Objects.equals(user,student)?Optional.of(course):Optional.empty(),
                (user,slug,id)->Optional.empty()
        );
        LearningCourse result = service.viewCourse(student, "java-basico");
        assertEquals(2, result.requiredLessons());
        assertEquals(1, result.completedRequiredLessons());
        assertEquals(50, result.progressPercent());
        assertEquals(3, result.modules().getFirst().lessons().size());
    }

    @Test
    void aulasOpcionaisNaoAumentamOProgressoObrigatorio() {
        LearningCourse course = new LearningCourse(courseId, "java-basico", "Java", List.of(
            new LearningModule(UUID.randomUUID(), "Módulo", 1, List.of(
                new Lesson(lessonId, "Extra", 1, LessonKind.TEXT, null,
                           "Conteúdo opcional", 30, false, true)
            ))
        ));
        assertEquals(0, course.requiredLessons());
        assertEquals(0, course.progressPercent());
    }

    @Test
    void rejeitaAcessoDeUsuarioSemMatricula() {
        var service = new StudentLearningService((user,slug)->Optional.empty(),
                                                 (user,slug,id)->Optional.empty());
        assertThrows(LearningCourseUnavailableException.class,
                     () -> service.viewCourse(student, "curso-inexistente"));
        assertThrows(LessonUnavailableException.class,
                     () -> service.completeLesson(student, "java-basico", lessonId));
    }

    @Test
    void marcarAulaNovamenteNaoGeraOutraConclusao() {
        LessonProgressWriterPort writer = new LessonProgressWriterPort() {
            private boolean alreadyCompleted;
            public Optional<LessonCompletionReceipt> markCompleted(UUID user, String slug, UUID id) {
                assertEquals(student, user);
                assertEquals("java-basico", slug);
                assertEquals(lessonId, id);
                boolean created = !alreadyCompleted;
                alreadyCompleted = true;
                return Optional.of(new LessonCompletionReceipt(id, created));
            }
        };
        var service = new StudentLearningService((user,slug)->Optional.empty(), writer);
        assertTrue(service.completeLesson(student, "java-basico", lessonId).newlyCompleted());
        assertFalse(service.completeLesson(student, "java-basico", lessonId).newlyCompleted());
    }

    @Test
    void validaSlugAntesDeConsultarPersistencia() {
        StudentLearningService service = new StudentLearningService(
                (user,slug)->fail("Leitura indevida"),
                (user,slug,id)->fail("Escrita indevida")
        );
        assertThrows(InvalidLearningRequestException.class, () -> service.viewCourse(student, "../outro"));
        assertThrows(InvalidLearningRequestException.class, () -> service.completeLesson(student, "Curso Java", lessonId));
    }
}
