package br.com.cursohub.enrollment.application;

import static org.junit.jupiter.api.Assertions.*;

import br.com.cursohub.enrollment.application.exception.InvalidEnrollmentRequestException;
import br.com.cursohub.enrollment.application.model.EnrollmentReceipt;
import br.com.cursohub.enrollment.application.port.out.EnrollmentWriterPort;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EnrollStudentServiceTest {
    private final UUID student = UUID.randomUUID();
    private final UUID course = UUID.randomUUID();
    private final UUID enrollment = UUID.randomUUID();

    @Test
    void estudanteAutenticadoSeMatriculaEmCursoPublicado() {
        EnrollmentWriterPort fake = (user, slug) -> {
            assertEquals(student, user);
            assertEquals("java-backend-do-zero", slug);
            return new EnrollmentReceipt(enrollment, course, slug, "ACTIVE", true);
        };
        var receipt = new EnrollStudentService(fake).execute(student, "java-backend-do-zero");
        assertTrue(receipt.created());
        assertEquals("ACTIVE", receipt.status());
    }

    @Test
    void segundaTentativaRetornaMatriculaExistente() {
        EnrollmentWriterPort fake = (user, slug) ->
                new EnrollmentReceipt(enrollment, course, slug, "ACTIVE", false);
        var receipt = new EnrollStudentService(fake).execute(student, "java-backend-do-zero");
        assertFalse(receipt.created());
        assertEquals(enrollment, receipt.enrollmentId());
    }

    @Test
    void rejeitaSlugInvalidoAntesDoBanco() {
        EnrollmentWriterPort fake = (user, slug) -> {
            fail("Não deveria acessar persistência");
            return null;
        };
        var service = new EnrollStudentService(fake);
        assertThrows(InvalidEnrollmentRequestException.class, () -> service.execute(student, "../outro"));
        assertThrows(InvalidEnrollmentRequestException.class, () -> service.execute(student, ""));
        assertThrows(InvalidEnrollmentRequestException.class, () -> service.execute(student, "Curso Java"));
    }

    @Test
    void naoAceitaIdentidadeAusente() {
        var service = new EnrollStudentService((user, slug) -> null);
        assertThrows(NullPointerException.class, () -> service.execute(null, "java-21"));
    }
}
