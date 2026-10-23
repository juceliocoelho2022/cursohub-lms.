package br.com.cursohub.assessment.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

/**
 * Pure scoring domain service. Correct options are loaded by the backend, never
 * accepted as a claim from the student's HTTP request.
 */
public final class AssessmentScoringService {
    public record Question(UUID id, BigDecimal points, UUID correctOptionId,
                           Set<UUID> allowedOptionIds) {
        public Question {
            Objects.requireNonNull(id);
            Objects.requireNonNull(points);
            Objects.requireNonNull(correctOptionId);
            allowedOptionIds = Set.copyOf(Objects.requireNonNull(allowedOptionIds));
            if (points.signum() <= 0 || allowedOptionIds.size() < 2
                    || !allowedOptionIds.contains(correctOptionId)) {
                throw new IllegalArgumentException("Questao invalida");
            }
        }
    }

    public record Answer(UUID questionId, UUID selectedOptionId) {
        public Answer {
            Objects.requireNonNull(questionId);
            Objects.requireNonNull(selectedOptionId);
        }
    }

    public record Result(BigDecimal score, boolean passed, int correctAnswers, int totalQuestions) {}

    public Result calculate(List<Question> questions, List<Answer> answers,
                            BigDecimal passingScore) {
        Objects.requireNonNull(questions);
        Objects.requireNonNull(answers);
        Objects.requireNonNull(passingScore);
        if (questions.isEmpty() || passingScore.compareTo(BigDecimal.ZERO) < 0
                || passingScore.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Avaliacao invalida");
        }

        Map<UUID, Question> byId = new HashMap<>();
        BigDecimal maxPoints = BigDecimal.ZERO;
        for (Question question : questions) {
            if (byId.putIfAbsent(question.id(), question) != null) {
                throw new IllegalArgumentException("Questao duplicada na avaliacao");
            }
            maxPoints = maxPoints.add(question.points());
        }

        Set<UUID> answered = new HashSet<>();
        BigDecimal earned = BigDecimal.ZERO;
        int correct = 0;
        for (Answer answer : answers) {
            Question question = byId.get(answer.questionId());
            if (question == null || !answered.add(answer.questionId())
                    || !question.allowedOptionIds().contains(answer.selectedOptionId())) {
                throw new IllegalArgumentException("Resposta duplicada ou opcao nao pertence a questao");
            }
            if (question.correctOptionId().equals(answer.selectedOptionId())) {
                earned = earned.add(question.points());
                correct++;
            }
        }
        if (answered.size() != byId.size()) {
            throw new IllegalArgumentException("Todas as questoes devem ser respondidas");
        }

        BigDecimal score = earned.multiply(new BigDecimal("100"))
                .divide(maxPoints, 2, RoundingMode.HALF_UP);
        return new Result(score, score.compareTo(passingScore) >= 0, correct, questions.size());
    }
}
