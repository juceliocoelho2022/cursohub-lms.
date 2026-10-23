package br.com.cursohub.assessment.application;

import br.com.cursohub.assessment.application.model.AssessmentModels.*;
import br.com.cursohub.assessment.application.port.in.AssessmentUseCase;
import br.com.cursohub.assessment.application.port.out.AssessmentStorePort;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Regras de autoria e validações antes da operação transacional do adaptador. */
public final class AssessmentService implements AssessmentUseCase {
    private final AssessmentStorePort store;
    public AssessmentService(AssessmentStorePort store) { this.store=Objects.requireNonNull(store); }

    @Override public StudentQuiz create(UUID instructor, UUID course, CreateQuiz draft) {
        Objects.requireNonNull(instructor);
        Objects.requireNonNull(course);
        if(draft==null || draft.title()==null || draft.title().strip().length()<5
                || draft.title().strip().length()>180)
            throw new AssessmentValidationException("Título deve ter de 5 a 180 caracteres");
        if(draft.passingScore()<1 || draft.passingScore()>100)
            throw new AssessmentValidationException("Nota mínima deve ficar entre 1 e 100");
        if(draft.maxAttempts()<1 || draft.maxAttempts()>10)
            throw new AssessmentValidationException("Limite de tentativas deve ficar entre 1 e 10");
        if(draft.questions()==null || draft.questions().isEmpty() || draft.questions().size()>20)
            throw new AssessmentValidationException("Avaliação deve ter entre 1 e 20 questões");
        for(QuestionDraft q:draft.questions()) {
            if(q==null || q.statement()==null || q.statement().strip().length()<5
                    || q.statement().length()>2000)
                throw new AssessmentValidationException("Cada questão exige um enunciado de 5 a 2000 caracteres");
            if(q.options()==null || q.options().size()<2 || q.options().size()>5)
                throw new AssessmentValidationException("Cada questão precisa de 2 a 5 alternativas");
            long correct=q.options().stream().filter(o->o!=null && o.correct()).count();
            if(correct!=1) throw new AssessmentValidationException("Cada questão deve ter uma alternativa correta");
            for(OptionDraft o:q.options())
                if(o==null || o.text()==null || o.text().strip().length()<1 || o.text().length()>500)
                    throw new AssessmentValidationException("Alternativas devem ter de 1 a 500 caracteres");
        }
        return store.save(instructor,course,draft);
    }
    @Override public Optional<StudentQuiz> findForInstructor(UUID instructor, UUID course) {
        return store.findForInstructor(Objects.requireNonNull(instructor),Objects.requireNonNull(course));
    }
    @Override public Optional<StudentQuiz> findForStudent(UUID student,String slug) {
        return store.findForStudent(Objects.requireNonNull(student),validSlug(slug));
    }
    @Override public AttemptResult submit(UUID student,String slug,UUID assessment,SubmitAnswers answers) {
        if(answers==null || answers.answers()==null)
            throw new AssessmentValidationException("Responda a todas as questões");
        if(answers.answers().stream().anyMatch(a->a==null || a.optionId()==null || a.questionId()==null))
            throw new AssessmentValidationException("Alternativa inválida");
        return store.submit(Objects.requireNonNull(student),validSlug(slug),
                Objects.requireNonNull(assessment),answers);
    }
    @Override public Certificate issue(UUID student,String slug) {
        return store.issue(Objects.requireNonNull(student),validSlug(slug));
    }
    @Override public Optional<Certificate> findCertificate(UUID student,String slug) {
        return store.findCertificate(Objects.requireNonNull(student),validSlug(slug));
    }
    @Override public Optional<Certificate> verify(UUID code) {
        return store.verify(Objects.requireNonNull(code));
    }
    private String validSlug(String slug) {
        if(slug==null || slug.length()>160 || !slug.matches("^[a-z0-9]+(?:-[a-z0-9]+)*$"))
            throw new AssessmentValidationException("Identificador do curso inválido");
        return slug;
    }
}
