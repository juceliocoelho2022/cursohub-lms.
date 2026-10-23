package br.com.cursohub.assessment.adapters.out;

import br.com.cursohub.assessment.application.*;
import br.com.cursohub.assessment.application.model.AssessmentModels.*;
import br.com.cursohub.assessment.application.port.out.AssessmentStorePort;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Banco relacional é a autoridade para permissões, correção e certificação. */
@Repository
public class AssessmentPostgresAdapter implements AssessmentStorePort {
    private final JdbcTemplate jdbc;
    public AssessmentPostgresAdapter(JdbcTemplate jdbc) { this.jdbc=jdbc; }

    private record QuizRow(UUID id,String title,int passingScore,int maxAttempts){}
    private record Enrollment(UUID id, UUID courseId, String status){}
    private record OptionRow(UUID questionId,UUID optionId,boolean correct){}

    @Override
    @Transactional
    public StudentQuiz save(UUID instructor,UUID courseId,CreateQuiz draft) {
        var courses=jdbc.query("""
            SELECT status FROM courses WHERE id=? AND instructor_id=? FOR UPDATE
            """,(rs,i)->rs.getString(1),courseId,instructor);
        if(courses.isEmpty()) throw new AssessmentNotFoundException();
        if(!"RASCUNHO".equals(courses.getFirst()))
            throw new AssessmentConflictException("Avaliações só podem ser cadastradas antes da publicação");
        Long count=jdbc.queryForObject("SELECT COUNT(*) FROM assessments WHERE course_id=?",Long.class,courseId);
        if(count!=null && count>0)
            throw new AssessmentConflictException("Este curso já possui uma avaliação final");
        UUID id=UUID.randomUUID();
        jdbc.update("""
            INSERT INTO assessments(id,course_id,title,passing_score,max_attempts,is_required)
            VALUES (?,?,?,?,?,TRUE)
            """,id,courseId,draft.title().strip(),draft.passingScore(),draft.maxAttempts());
        int n=0;
        for(QuestionDraft question:draft.questions()) {
            UUID qid=UUID.randomUUID();
            jdbc.update("""
                INSERT INTO assessment_questions(id,assessment_id,statement,position,points)
                VALUES (?,?,?,?,1)
                """,qid,id,question.statement().strip(),++n);
            int m=0;
            for(OptionDraft option:question.options())
                jdbc.update("""
                    INSERT INTO question_options(id,question_id,option_text,position,is_correct)
                    VALUES (?,?,?,?,?)
                    """,UUID.randomUUID(),qid,option.text().strip(),++m,option.correct());
        }
        return findQuiz(id,0,false);
    }

    @Override
    @Transactional(readOnly=true)
    public Optional<StudentQuiz> findForInstructor(UUID instructor,UUID course) {
        var rows=jdbc.query("""
            SELECT a.id,a.title,a.passing_score,a.max_attempts FROM assessments a
            JOIN courses c ON c.id=a.course_id WHERE c.id=? AND c.instructor_id=?
            """,(rs,i)->quizRow(rs),course,instructor);
        return rows.stream().findFirst().map(a->findQuiz(a.id(),0,false));
    }

    @Override
    @Transactional(readOnly=true)
    public Optional<StudentQuiz> findForStudent(UUID student,String slug) {
        var rows=jdbc.query("""
            SELECT a.id,a.title,a.passing_score,a.max_attempts,e.id AS enrollment_id
            FROM enrollments e JOIN courses c ON c.id=e.course_id
            JOIN assessments a ON a.course_id=c.id
            WHERE e.student_id=? AND c.slug=? AND e.status IN ('ACTIVE','COMPLETED')
            """,(rs,i)->new AbstractMap.SimpleEntry<>(quizRow(rs),
                    rs.getObject("enrollment_id",UUID.class)),student,slug);
        if(rows.isEmpty()) return Optional.empty();
        var row=rows.getFirst();
        Long attempts=jdbc.queryForObject("""
            SELECT COUNT(*) FROM assessment_attempts WHERE assessment_id=? AND enrollment_id=?
            """,Long.class,row.getKey().id(),row.getValue());
        Boolean passed=jdbc.queryForObject("""
            SELECT EXISTS(SELECT 1 FROM assessment_attempts
              WHERE assessment_id=? AND enrollment_id=? AND passed=TRUE)
            """,Boolean.class,row.getKey().id(),row.getValue());
        return Optional.of(findQuiz(row.getKey().id(),Math.toIntExact(attempts),Boolean.TRUE.equals(passed)));
    }

    @Override
    @Transactional
    public AttemptResult submit(UUID student,String slug,UUID assessmentId,SubmitAnswers answers) {
        Enrollment enrollment=lockEnrollment(student,slug);
        if(!"ACTIVE".equals(enrollment.status()))
            throw new AssessmentConflictException("Matrícula não está ativa para novas tentativas");
        QuizRow quiz=findOwnedAssessment(assessmentId,enrollment.courseId());
        checkRequiredLessons(enrollment);
        Long attempts=jdbc.queryForObject("""
            SELECT COUNT(*) FROM assessment_attempts WHERE assessment_id=? AND enrollment_id=?
            """,Long.class,assessmentId,enrollment.id());
        if(attempts==null) throw new IllegalStateException("Contador de tentativas indisponível");
        Boolean alreadyPassed=jdbc.queryForObject("""
            SELECT EXISTS(SELECT 1 FROM assessment_attempts
               WHERE assessment_id=? AND enrollment_id=? AND passed=TRUE)
            """,Boolean.class,assessmentId,enrollment.id());
        if(Boolean.TRUE.equals(alreadyPassed))
            throw new AssessmentConflictException("Avaliação já aprovada");
        if(attempts>=quiz.maxAttempts())
            throw new AssessmentConflictException("Limite de tentativas atingido");

        List<OptionRow> allOptions=jdbc.query("""
            SELECT q.id AS question_id,o.id AS option_id,o.is_correct
            FROM assessment_questions q JOIN question_options o ON o.question_id=q.id
            WHERE q.assessment_id=?
            """,(rs,i)->new OptionRow(rs.getObject("question_id",UUID.class),
                    rs.getObject("option_id",UUID.class),rs.getBoolean("is_correct")),assessmentId);

        Map<UUID,Map<UUID,Boolean>> valid=new HashMap<>();
        for(var opt:allOptions)
            valid.computeIfAbsent(opt.questionId(),key->new HashMap<>())
                 .put(opt.optionId(),opt.correct());
        List<Choice> chosen=answers.answers();
        if(valid.isEmpty() || chosen.size()!=valid.size())
            throw new AssessmentValidationException("Responda a todas as questões, uma vez cada");
        Set<UUID> seen=new HashSet<>();
        int correct=0;
        for(var choice:chosen) {
            if(!seen.add(choice.questionId())) throw new AssessmentValidationException("Questão repetida");
            var options=valid.get(choice.questionId());
            if(options==null || !options.containsKey(choice.optionId()))
                throw new AssessmentValidationException("Alternativa não pertence à avaliação");
            if(Boolean.TRUE.equals(options.get(choice.optionId()))) correct++;
        }
        BigDecimal score=BigDecimal.valueOf(correct*100L).divide(
            BigDecimal.valueOf(valid.size()),2,RoundingMode.HALF_UP);
        boolean passed=score.compareTo(BigDecimal.valueOf(quiz.passingScore()))>=0;
        int number=Math.toIntExact(attempts)+1;
        UUID attempt=UUID.randomUUID();
        jdbc.update("""
          INSERT INTO assessment_attempts(id,assessment_id,enrollment_id,attempt_number,
            score,passed,submitted_at)
          VALUES (?,?,?,?,?,?,now())
          """,attempt,assessmentId,enrollment.id(),number,score,passed);
        for(var choice:chosen)
            jdbc.update("""
              INSERT INTO assessment_answers(attempt_id,question_id,selected_option_id)
              VALUES (?,?,?)
              """,attempt,choice.questionId(),choice.optionId());
        return new AttemptResult(attempt,number,score,passed,quiz.maxAttempts()-number);
    }

    @Override
    @Transactional
    public Certificate issue(UUID student,String slug) {
        Enrollment enrollment=lockEnrollment(student,slug);
        if(!List.of("ACTIVE","COMPLETED").contains(enrollment.status()))
            throw new AssessmentConflictException("Matrícula não está ativa");
        checkRequiredLessons(enrollment);
        Long missing=jdbc.queryForObject("""
            SELECT COUNT(*) FROM assessments a
            WHERE a.course_id=? AND a.is_required=TRUE
              AND NOT EXISTS(SELECT 1 FROM assessment_attempts t
                WHERE t.assessment_id=a.id AND t.enrollment_id=? AND t.passed=TRUE)
            """,Long.class,enrollment.courseId(),enrollment.id());
        if(missing!=null && missing>0)
            throw new AssessmentConflictException("Conclua e aprove as avaliações obrigatórias");
        var revoked=jdbc.query("""
            SELECT revoked_at IS NOT NULL FROM certificates WHERE enrollment_id=?
            """,(rs,i)->rs.getBoolean(1),enrollment.id());
        if(!revoked.isEmpty() && Boolean.TRUE.equals(revoked.getFirst()))
            throw new AssessmentConflictException("Este certificado foi revogado");
        jdbc.update("""
            INSERT INTO certificates(enrollment_id) VALUES (?)
            ON CONFLICT(enrollment_id) DO NOTHING
            """,enrollment.id());
        jdbc.update("""
            UPDATE enrollments SET status='COMPLETED',completed_at=COALESCE(completed_at,now())
            WHERE id=? AND status='ACTIVE'
            """,enrollment.id());
        return byEnrollment(enrollment.id()).orElseThrow(AssessmentNotFoundException::new);
    }

    @Override
    @Transactional(readOnly=true)
    public Optional<Certificate> findCertificate(UUID student,String slug) {
        var ids=jdbc.query("""
            SELECT e.id FROM enrollments e JOIN courses c ON c.id=e.course_id
            WHERE e.student_id=? AND c.slug=?
            AND e.status='COMPLETED'
            """,(rs,i)->rs.getObject(1,UUID.class),student,slug);
        return ids.isEmpty()?Optional.empty():byEnrollment(ids.getFirst());
    }

    @Override
    @Transactional(readOnly=true)
    public Optional<Certificate> verify(UUID code) {
        var rows=jdbc.query("""
            SELECT cert.id,cert.verification_code,cert.issued_at,
                   u.full_name AS student_name,c.title AS course_title
            FROM certificates cert JOIN enrollments e ON e.id=cert.enrollment_id
            JOIN app_users u ON u.id=e.student_id JOIN courses c ON c.id=e.course_id
            WHERE cert.verification_code=? AND cert.revoked_at IS NULL
            AND e.status='COMPLETED'
            """,(rs,i)->certificate(rs),code);
        return rows.stream().findFirst();
    }

    private Optional<Certificate> byEnrollment(UUID id) {
        var rows=jdbc.query("""
            SELECT cert.id,cert.verification_code,cert.issued_at,
                   u.full_name AS student_name,c.title AS course_title
            FROM certificates cert JOIN enrollments e ON e.id=cert.enrollment_id
            JOIN app_users u ON u.id=e.student_id JOIN courses c ON c.id=e.course_id
            WHERE cert.enrollment_id=? AND cert.revoked_at IS NULL
            """,(rs,i)->certificate(rs),id);
        return rows.stream().findFirst();
    }
    private static Certificate certificate(ResultSet rs)throws SQLException {
        return new Certificate(rs.getObject("id",UUID.class),
             rs.getObject("verification_code",UUID.class),rs.getString("student_name"),
             rs.getString("course_title"),rs.getTimestamp("issued_at").toInstant());
    }
    private Enrollment lockEnrollment(UUID student,String slug) {
        var rows=jdbc.query("""
            SELECT e.id,e.course_id,e.status FROM enrollments e
            JOIN courses c ON c.id=e.course_id
            WHERE e.student_id=? AND c.slug=?
            FOR UPDATE OF e
            """,(rs,i)->new Enrollment(rs.getObject("id",UUID.class),
                    rs.getObject("course_id",UUID.class),rs.getString("status")),student,slug);
        if(rows.isEmpty()) throw new AssessmentNotFoundException();
        return rows.getFirst();
    }
    private void checkRequiredLessons(Enrollment enrollment) {
        Long required=jdbc.queryForObject("""
          SELECT COUNT(*) FROM lessons l JOIN course_modules m ON m.id=l.module_id
          WHERE m.course_id=? AND (l.is_required=TRUE OR l.kind='VIDEO')
          """,Long.class,enrollment.courseId());
        Long missing=jdbc.queryForObject("""
          SELECT COUNT(*) FROM lessons l JOIN course_modules m ON m.id=l.module_id
          LEFT JOIN lesson_progress lp ON lp.lesson_id=l.id AND lp.enrollment_id=?
          WHERE m.course_id=? AND (l.is_required=TRUE OR l.kind='VIDEO') AND lp.id IS NULL
          """,Long.class,enrollment.id(),enrollment.courseId());
        if(required==null || required==0 || missing==null || missing>0)
            throw new AssessmentConflictException("Conclua todas as aulas obrigatórias antes de continuar");
    }
    private QuizRow findOwnedAssessment(UUID assessment,UUID course) {
        var rows=jdbc.query("""
          SELECT id,title,passing_score,max_attempts FROM assessments
          WHERE id=? AND course_id=?
          """,(rs,i)->quizRow(rs),assessment,course);
        if(rows.isEmpty()) throw new AssessmentNotFoundException();
        return rows.getFirst();
    }
    private static QuizRow quizRow(ResultSet rs)throws SQLException {
        return new QuizRow(rs.getObject("id",UUID.class),rs.getString("title"),
             rs.getBigDecimal("passing_score").intValue(),rs.getInt("max_attempts"));
    }
    private StudentQuiz findQuiz(UUID id,int attempts,boolean passed) {
        QuizRow assessment=jdbc.query("""
            SELECT id,title,passing_score,max_attempts FROM assessments WHERE id=?
            """,(rs,i)->quizRow(rs),id).getFirst();
        var qs=jdbc.query("""
            SELECT id,statement FROM assessment_questions
            WHERE assessment_id=? ORDER BY position
            """,(rs,i)->new AbstractMap.SimpleEntry<>(
                rs.getObject("id",UUID.class),rs.getString("statement")),id);
        List<Question> questions=new ArrayList<>();
        for(var q:qs){
            List<Option> opts=jdbc.query("""
              SELECT id,option_text FROM question_options WHERE question_id=? ORDER BY position
              """,(rs,i)->new Option(rs.getObject("id",UUID.class),rs.getString("option_text")),q.getKey());
            questions.add(new Question(q.getKey(),q.getValue(),List.copyOf(opts)));
        }
        return new StudentQuiz(assessment.id(),assessment.title(),assessment.passingScore(),
                    assessment.maxAttempts(),attempts,passed,List.copyOf(questions));
    }
}
