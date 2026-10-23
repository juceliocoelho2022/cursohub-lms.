package br.com.cursohub.assessment.application;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;

class AssessmentAttemptPolicyTest {
    private final AssessmentAttemptPolicy policy = new AssessmentAttemptPolicy();

    @Test void primeiraTentativa() {
        assertEquals(1, policy.nextAttemptNumber(List.of(), 3));
    }

    @Test void liberaNovaTentativaAposReprovacao() {
        assertEquals(2, policy.nextAttemptNumber(
                List.of(new AssessmentAttemptPolicy.Attempt(1, true, false)), 3));
    }

    @Test void impedeTentativaAposAprovacao() {
        assertThrows(IllegalStateException.class, () -> policy.nextAttemptNumber(
                List.of(new AssessmentAttemptPolicy.Attempt(1, true, true)), 3));
    }

    @Test void impedeExcederLimite() {
        assertThrows(IllegalStateException.class, () -> policy.nextAttemptNumber(
                List.of(new AssessmentAttemptPolicy.Attempt(1, true, false)), 1));
    }

    @Test void bloqueiaTentativaEmAndamento() {
        assertThrows(IllegalStateException.class, () -> policy.nextAttemptNumber(
                List.of(new AssessmentAttemptPolicy.Attempt(1, false, false)), 3));
    }

    @Test void rejeitaHistoricoInconsistente() {
        assertThrows(IllegalArgumentException.class, () -> policy.nextAttemptNumber(
                List.of(new AssessmentAttemptPolicy.Attempt(2, true, false)), 3));
        assertThrows(IllegalArgumentException.class, () -> policy.nextAttemptNumber(
                List.of(new AssessmentAttemptPolicy.Attempt(1, true, false),
                        new AssessmentAttemptPolicy.Attempt(1, true, false)), 3));
    }
}
