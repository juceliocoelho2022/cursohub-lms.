package br.com.cursohub.assessment.application;

import java.util.List;
import java.util.Objects;

/**
 * Deterministic domain policy for new assessment attempts.
 * Persistence adapters must still lock the enrollment row and use the
 * (enrollment_id, assessment_id, attempt_number) unique constraint.
 */
public final class AssessmentAttemptPolicy {
    public record Attempt(int number, boolean submitted, boolean passed) {
        public Attempt {
            if (number < 1 || (passed && !submitted)) {
                throw new IllegalArgumentException("Tentativa invalida");
            }
        }
    }

    public int nextAttemptNumber(List<Attempt> history, int maxAttempts) {
        Objects.requireNonNull(history);
        if (maxAttempts < 1) throw new IllegalArgumentException("Limite invalido");
        if (history.size() > maxAttempts)
            throw new IllegalStateException("Historico excede o limite configurado");

        boolean[] seen = new boolean[maxAttempts + 1];
        for (Attempt attempt : history) {
            if (attempt == null || attempt.number() > maxAttempts || seen[attempt.number()]) {
                throw new IllegalArgumentException("Historico de tentativas inconsistente");
            }
            seen[attempt.number()] = true;
            if (!attempt.submitted())
                throw new IllegalStateException("Tentativa em andamento deve ser concluida");
            if (attempt.passed())
                throw new IllegalStateException("Avaliacao ja aprovada");
        }
        if (history.size() >= maxAttempts) throw new IllegalStateException("Tentativas esgotadas");
        // History is sequential; a missing number indicates invalid state.
        for (int i = 1; i <= history.size(); i++) {
            if (!seen[i]) throw new IllegalArgumentException("Tentativas fora de sequencia");
        }
        return history.size() + 1;
    }
}
