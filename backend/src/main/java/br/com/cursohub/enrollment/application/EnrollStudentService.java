package br.com.cursohub.enrollment.application;

import br.com.cursohub.enrollment.application.exception.InvalidEnrollmentRequestException;
import br.com.cursohub.enrollment.application.model.EnrollmentReceipt;
import br.com.cursohub.enrollment.application.port.in.EnrollStudentUseCase;
import br.com.cursohub.enrollment.application.port.out.EnrollmentWriterPort;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/** Caso de uso puro, independente de Spring, SQL e HTTP. */
public final class EnrollStudentService implements EnrollStudentUseCase {
    private static final Pattern SLUG = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");
    private final EnrollmentWriterPort writer;

    public EnrollStudentService(EnrollmentWriterPort writer) {
        this.writer = Objects.requireNonNull(writer);
    }

    @Override
    public EnrollmentReceipt execute(UUID studentId, String courseSlug) {
        Objects.requireNonNull(studentId, "Identidade autenticada obrigatória");
        if (courseSlug == null || courseSlug.length() > 160 || !SLUG.matcher(courseSlug).matches()) {
            throw new InvalidEnrollmentRequestException();
        }
        return writer.enrollIfPublished(studentId, courseSlug);
    }
}
