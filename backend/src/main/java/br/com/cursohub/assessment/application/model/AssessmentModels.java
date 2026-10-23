package br.com.cursohub.assessment.application.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Contratos independentes de Spring; gabarito nunca é enviado no modelo StudentQuiz. */
public final class AssessmentModels {
    private AssessmentModels() {}
    public record OptionDraft(String text, boolean correct) {}
    public record QuestionDraft(String statement, List<OptionDraft> options) {}
    public record CreateQuiz(String title, int passingScore, int maxAttempts, List<QuestionDraft> questions) {}
    public record Option(UUID id, String text) {}
    public record Question(UUID id, String statement, List<Option> options) {}
    public record StudentQuiz(UUID id, String title, int passingScore, int maxAttempts,
                              int attemptsUsed, boolean passed, List<Question> questions) {}
    public record Choice(UUID questionId, UUID optionId) {}
    public record SubmitAnswers(List<Choice> answers) {}
    public record AttemptResult(UUID id, int number, BigDecimal score, boolean passed,
                                int attemptsRemaining) {}
    public record Certificate(UUID id, UUID verificationCode, String studentName, String courseTitle,
                              Instant issuedAt) {}
}
