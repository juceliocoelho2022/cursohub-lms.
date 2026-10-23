package br.com.cursohub.assessment.application;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AssessmentScoringServiceTest {
    private final AssessmentScoringService service = new AssessmentScoringService();
    private final UUID q1 = UUID.randomUUID();
    private final UUID q2 = UUID.randomUUID();
    private final UUID a1 = UUID.randomUUID();
    private final UUID b1 = UUID.randomUUID();
    private final UUID a2 = UUID.randomUUID();
    private final UUID b2 = UUID.randomUUID();

    private List<AssessmentScoringService.Question> questions() {
        return List.of(
            new AssessmentScoringService.Question(q1, new BigDecimal("3"), a1, Set.of(a1,b1)),
            new AssessmentScoringService.Question(q2, new BigDecimal("1"), a2, Set.of(a2,b2)));
    }

    @Test void notaPonderadaEhCalculadaNoServidor() {
        var result = service.calculate(questions(), List.of(
            new AssessmentScoringService.Answer(q1,a1),
            new AssessmentScoringService.Answer(q2,b2)), new BigDecimal("70"));
        assertEquals(new BigDecimal("75.00"), result.score());
        assertTrue(result.passed());
        assertEquals(1, result.correctAnswers());
        assertEquals(2, result.totalQuestions());
    }

    @Test void reprovaQuandoNaoAtingeNotaMinima() {
        var result = service.calculate(questions(), List.of(
            new AssessmentScoringService.Answer(q1,b1),
            new AssessmentScoringService.Answer(q2,a2)), new BigDecimal("70"));
        assertEquals(new BigDecimal("25.00"), result.score());
        assertFalse(result.passed());
    }

    @Test void rejeitaAlternativaDeOutraQuestao() {
        assertThrows(IllegalArgumentException.class, () -> service.calculate(questions(),
            List.of(new AssessmentScoringService.Answer(q1,a2),
                    new AssessmentScoringService.Answer(q2,b2)), new BigDecimal("70")));
    }

    @Test void rejeitaRespostasDuplicadas() {
        assertThrows(IllegalArgumentException.class, () -> service.calculate(questions(),
            List.of(new AssessmentScoringService.Answer(q1,a1),
                    new AssessmentScoringService.Answer(q1,b1)), new BigDecimal("70")));
    }

    @Test void exigeRespostasCompletas() {
        assertThrows(IllegalArgumentException.class, () -> service.calculate(questions(),
            List.of(new AssessmentScoringService.Answer(q1,a1)), new BigDecimal("70")));
    }

    @Test void impedeQuestoesDuplicadasNaProva() {
        var question = questions().getFirst();
        assertThrows(IllegalArgumentException.class, () -> service.calculate(
            List.of(question,question), List.of(new AssessmentScoringService.Answer(q1,a1)),
            new BigDecimal("70")));
    }

    @Test void rejeitaQuestaoSemAlternativaCorretaValida() {
        assertThrows(IllegalArgumentException.class, () ->
            new AssessmentScoringService.Question(q1, BigDecimal.ONE, a1, Set.of(a2,b2)));
    }
}
