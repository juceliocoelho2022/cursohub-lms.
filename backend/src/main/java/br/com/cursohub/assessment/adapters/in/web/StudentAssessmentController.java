package br.com.cursohub.assessment.adapters.in.web;

import br.com.cursohub.assessment.application.AssessmentAttemptPolicy;
import br.com.cursohub.assessment.application.AssessmentScoringService;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/** Student assessment API. Identity always comes from validated JWT. */
@RestController
@RequestMapping("/api/v1/students/me/assessments")
public class StudentAssessmentController {
    private final JdbcTemplate jdbc;
    private final AssessmentScoringService scoring = new AssessmentScoringService();
    private final AssessmentAttemptPolicy attempts = new AssessmentAttemptPolicy();

    public StudentAssessmentController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public record OptionView(UUID id, String text) {}
    public record QuestionView(UUID id, String statement, BigDecimal points, List<OptionView> options) {}
    public record AssessmentView(UUID id, String title, BigDecimal passingScore, int maxAttempts,
                                 List<QuestionView> questions) {}
    public record SubmittedAnswer(UUID questionId, UUID selectedOptionId) {}
    public record Submission(List<SubmittedAnswer> answers) {}
    public record Grade(UUID attemptId, int attemptNumber, BigDecimal score,
                        boolean passed, int correctAnswers, int totalQuestions) {}
    private record AssessmentInfo(UUID id, String title, BigDecimal passing, int maxAttempts) {}

    @GetMapping("/{assessmentId}")
    public AssessmentView view(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID assessmentId) {
        UUID studentId = UUID.fromString(jwt.getSubject());
        AssessmentInfo info = requireEnrolled(studentId, assessmentId, false);
        List<QuestionView> questions = loadQuestions(assessmentId);
        if (questions.isEmpty()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Avaliacao sem questoes");
        return new AssessmentView(info.id(), info.title(), info.passing(),
                info.maxAttempts(), questions);
    }

    @PostMapping("/{assessmentId}/submit")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Grade submit(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID assessmentId,
                        @RequestBody Submission submission) {
        UUID studentId = UUID.fromString(jwt.getSubject());
        // Lock the enrollment to serialize concurrent submissions for this student.
        AssessmentInfo info = requireEnrolled(studentId, assessmentId, true);
        UUID enrollmentId = jdbc.queryForObject("""
                SELECT e.id FROM enrollments e JOIN assessments a ON a.course_id = e.course_id
                WHERE e.student_id=? AND a.id=? AND e.status='ACTIVE'
                FOR UPDATE OF e
                """, UUID.class, studentId, assessmentId);
        if (enrollmentId == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        var history = jdbc.query("""
                SELECT attempt_number, submitted_at IS NOT NULL AS submitted, COALESCE(passed, false) AS passed
                FROM assessment_attempts WHERE assessment_id=? AND enrollment_id=? ORDER BY attempt_number
                """, (rs, n) -> new AssessmentAttemptPolicy.Attempt(
                rs.getInt("attempt_number"), rs.getBoolean("submitted"), rs.getBoolean("passed")),
                assessmentId, enrollmentId);
        final int number;
        try { number = attempts.nextAttemptNumber(history, info.maxAttempts()); }
        catch (IllegalStateException ex) { throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage()); }

        List<AssessmentScoringService.Question> questions = loadScoringQuestions(assessmentId);
        if (submission == null || submission.answers() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Respostas obrigatorias");
        List<AssessmentScoringService.Answer> answers;
        try {
            answers = submission.answers().stream().map(a ->
                    new AssessmentScoringService.Answer(a.questionId(), a.selectedOptionId())).toList();
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resposta invalida");
        }
        final AssessmentScoringService.Result result;
        try { result = scoring.calculate(questions, answers, info.passing()); }
        catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
        UUID attemptId = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO assessment_attempts
                (id, assessment_id, enrollment_id, attempt_number, score, passed, submitted_at)
                VALUES (?, ?, ?, ?, ?, ?, now())
                """, attemptId, assessmentId, enrollmentId, number, result.score(), result.passed());
        for (var answer : answers) {
            jdbc.update("""
                    INSERT INTO assessment_answers (attempt_id, question_id, selected_option_id)
                    VALUES (?, ?, ?)
                    """, attemptId, answer.questionId(), answer.selectedOptionId());
        }
        return new Grade(attemptId, number, result.score(), result.passed(),
                result.correctAnswers(), result.totalQuestions());
    }

    private AssessmentInfo requireEnrolled(UUID studentId, UUID assessmentId, boolean activeOnly) {
        String sql = """
                SELECT a.id, a.title, a.passing_score, a.max_attempts
                FROM assessments a JOIN courses c ON c.id=a.course_id
                JOIN enrollments e ON e.course_id=c.id
                WHERE a.id=? AND e.student_id=? AND c.status='PUBLICADO'
                  AND e.status IN ('ACTIVE', 'COMPLETED')
                """;
        var rows = jdbc.query(sql, (rs, n) -> new AssessmentInfo(
                rs.getObject("id", UUID.class), rs.getString("title"),
                rs.getBigDecimal("passing_score"), rs.getInt("max_attempts")),
                assessmentId, studentId);
        if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Avaliacao indisponivel");
        return rows.getFirst();
    }

    private List<QuestionView> loadQuestions(UUID assessmentId) {
        var questions = jdbc.query("""
                SELECT id, statement, points FROM assessment_questions
                WHERE assessment_id=? ORDER BY position
                """, (rs,n) -> new QuestionView(rs.getObject("id", UUID.class),
                rs.getString("statement"), rs.getBigDecimal("points"), new ArrayList<>()), assessmentId);
        for (var question : questions) {
            question.options().addAll(jdbc.query("""
                    SELECT id, option_text FROM question_options WHERE question_id=? ORDER BY position
                    """, (rs,n) -> new OptionView(rs.getObject("id", UUID.class),
                    rs.getString("option_text")), question.id()));
        }
        return questions;
    }

    private List<AssessmentScoringService.Question> loadScoringQuestions(UUID assessmentId) {
        var questions = jdbc.query("""
                SELECT id, points FROM assessment_questions WHERE assessment_id=? ORDER BY position
                """, (rs,n) -> Map.entry(rs.getObject("id", UUID.class), rs.getBigDecimal("points")), assessmentId);
        var result = new ArrayList<AssessmentScoringService.Question>();
        for (var question : questions) {
            var options = jdbc.query("""
                    SELECT id, is_correct FROM question_options WHERE question_id=?
                    """, (rs,n) -> Map.entry(rs.getObject("id", UUID.class), rs.getBoolean("is_correct")),
                    question.getKey());
            var correct = options.stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).toList();
            if (correct.size()!=1) throw new ResponseStatusException(HttpStatus.CONFLICT, "Gabarito incompleto");
            result.add(new AssessmentScoringService.Question(question.getKey(), question.getValue(),
                    correct.getFirst(), options.stream().map(Map.Entry::getKey)
                    .collect(java.util.stream.Collectors.toSet())));
        }
        return result;
    }
}
