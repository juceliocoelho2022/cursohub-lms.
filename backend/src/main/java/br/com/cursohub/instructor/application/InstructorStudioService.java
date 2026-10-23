package br.com.cursohub.instructor.application;

import br.com.cursohub.instructor.application.exception.InvalidInstructorContentException;
import br.com.cursohub.instructor.application.model.*;
import br.com.cursohub.instructor.application.port.in.InstructorStudioUseCase;
import br.com.cursohub.instructor.application.port.out.InstructorStudioPort;
import br.com.cursohub.instructor.domain.*;
import br.com.cursohub.learning.domain.LessonKind;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/** Validação em Java puro, sem dependência de framework ou banco. */
public final class InstructorStudioService implements InstructorStudioUseCase {
    private static final Pattern SLUG = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");
    private final InstructorStudioPort store;

    public InstructorStudioService(InstructorStudioPort store) {
        this.store = Objects.requireNonNull(store);
    }

    @Override
    public List<InstructorCourse> listCourses(UUID instructorId) {
        return store.listOwned(Objects.requireNonNull(instructorId));
    }

    @Override
    public InstructorCourseContent getCourse(UUID instructorId, UUID courseId) {
        return store.findOwned(Objects.requireNonNull(instructorId), Objects.requireNonNull(courseId));
    }

    @Override
    public InstructorCourse createCourse(UUID instructorId, CreateCourseCommand command) {
        Objects.requireNonNull(instructorId);
        if (command == null || command.level() == null ||
            command.slug() == null || command.slug().length() > 160 || !SLUG.matcher(command.slug()).matches())
            throw new InvalidInstructorContentException("Slug inválido: utilize minúsculas, números e hífens");
        String title = required(command.title(), 5, 180, "Título");
        String description = required(command.description(), 10, 20000, "Descrição");
        String category = required(command.category(), 2, 60, "Categoria");
        if (command.estimatedMinutes() < 0 || command.estimatedMinutes() > 100000)
            throw new InvalidInstructorContentException("Duração estimada inválida");
        return store.createDraft(instructorId, new CreateCourseCommand(
                command.slug(), title, description, category, command.level(), command.estimatedMinutes()));
    }

    @Override
    public InstructorModule addModule(UUID instructorId, UUID courseId, String title) {
        return store.appendModule(Objects.requireNonNull(instructorId), Objects.requireNonNull(courseId),
                required(title, 3, 180, "Título do módulo"));
    }

    @Override
    public InstructorLesson addLesson(UUID instructorId, UUID courseId, UUID moduleId, CreateLessonCommand command) {
        Objects.requireNonNull(instructorId);
        Objects.requireNonNull(courseId);
        Objects.requireNonNull(moduleId);
        if (command == null || command.kind() == null)
            throw new InvalidInstructorContentException("Tipo de aula obrigatório");
        String title = required(command.title(), 3, 180, "Título da aula");
        if (command.durationSeconds() < 0 || command.durationSeconds() > 720000)
            throw new InvalidInstructorContentException("Duração da aula inválida");
        String bodyText = trimNullable(command.bodyText());
        String url = trimNullable(command.contentUrl());
        if (command.kind() == LessonKind.TEXT) {
            if (bodyText == null || bodyText.length() < 5)
                throw new InvalidInstructorContentException("Aula de texto exige conteúdo com ao menos 5 caracteres");
            if (bodyText.length() > 100000)
                throw new InvalidInstructorContentException("Conteúdo da aula é muito longo");
            url = null;
        } else {
            if ((command.kind() == LessonKind.PDF && url == null) ||
                    (url != null && (url.length() > 1000 || !validHttps(url))))
                throw new InvalidInstructorContentException("Materiais externos exigem URL HTTPS válida");
            // VIDEO sem URL aguarda upload seguro de MP4/WEBM no próprio curso.
            bodyText = null;
        }
        return store.appendLesson(instructorId, courseId, moduleId,
                new CreateLessonCommand(title, command.kind(), url, bodyText,
                                        command.durationSeconds(), command.required()));
    }

    @Override
    public InstructorCourse publishCourse(UUID instructorId, UUID courseId) {
        return store.publish(Objects.requireNonNull(instructorId), Objects.requireNonNull(courseId));
    }

    private static String required(String input, int min, int max, String label) {
        if (input == null) throw new InvalidInstructorContentException(label + " obrigatório");
        String value = input.strip();
        if (value.length() < min || value.length() > max)
            throw new InvalidInstructorContentException(label + " deve ter entre " + min + " e " + max + " caracteres");
        return value;
    }

    private static String trimNullable(String value) {
        if (value == null || value.isBlank()) return null;
        return value.strip();
    }

    private static boolean validHttps(String input) {
        try {
            URI uri = URI.create(input);
            return "https".equalsIgnoreCase(uri.getScheme())
                   && uri.getHost() != null && !uri.getHost().isBlank()
                   && uri.getUserInfo() == null && uri.getFragment() == null;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
