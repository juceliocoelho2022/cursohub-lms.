package br.com.cursohub.assessment.application;

import static org.junit.jupiter.api.Assertions.*;
import br.com.cursohub.assessment.application.model.AssessmentModels.*;
import br.com.cursohub.assessment.application.port.out.AssessmentStorePort;
import java.util.*;
import org.junit.jupiter.api.Test;

class AssessmentServiceTest {
    private final UUID instructor=UUID.randomUUID(),course=UUID.randomUUID();
    private final RecordingStore store=new RecordingStore();
    private final AssessmentService service=new AssessmentService(store);

    @Test void permiteUmaAvaliacaoValidaComGabaritoUnico() {
        var quiz=new CreateQuiz("Avaliação Final",70,3,List.of(
            new QuestionDraft("O que é a JVM?",List.of(
                new OptionDraft("Máquina virtual Java",true),
                new OptionDraft("Banco de dados",false),
                new OptionDraft("Servidor de e-mail",false)))
        ));
        service.create(instructor,course,quiz);
        assertEquals(instructor,store.instructor);
        assertEquals(course,store.course);
        assertEquals(70,store.draft.passingScore());
    }
    @Test void rejeitaQuestaoComDuasRespostasCorretas() {
        assertThrows(AssessmentValidationException.class,()->service.create(instructor,course,
            new CreateQuiz("Avaliação Final",70,3,List.of(new QuestionDraft("Pergunta sobre Java",List.of(
                new OptionDraft("Java",true),new OptionDraft("Spring",true)))))));
        assertNull(store.draft);
    }
    @Test void rejeitaNotaInvalidaETentativasExcessivas() {
        var questions=List.of(new QuestionDraft("Pergunta sobre Java",List.of(
            new OptionDraft("Java",true),new OptionDraft("C",false))));
        assertThrows(AssessmentValidationException.class,
            ()->service.create(instructor,course,new CreateQuiz("Avaliação Final",0,3,questions)));
        assertThrows(AssessmentValidationException.class,
            ()->service.create(instructor,course,new CreateQuiz("Avaliação Final",70,11,questions)));
    }
    @Test void naoAceitaRespostasNulasOuSlugComTravessia() {
        assertThrows(AssessmentValidationException.class,
            ()->service.submit(UUID.randomUUID(),"../../admin",UUID.randomUUID(),
                              new SubmitAnswers(List.of())));
        assertThrows(AssessmentValidationException.class,
            ()->service.submit(UUID.randomUUID(),"curso-java",UUID.randomUUID(),
                              new SubmitAnswers(List.of(new Choice(null,UUID.randomUUID())))));
    }
    private static final class RecordingStore implements AssessmentStorePort {
        UUID instructor,course;
        CreateQuiz draft;
        public StudentQuiz save(UUID i,UUID c,CreateQuiz q) {
            instructor=i;course=c;draft=q;
            return new StudentQuiz(UUID.randomUUID(),q.title(),q.passingScore(),q.maxAttempts(),0,false,List.of());
        }
        public Optional<StudentQuiz> findForInstructor(UUID i,UUID c){return Optional.empty();}
        public Optional<StudentQuiz> findForStudent(UUID i,String slug){return Optional.empty();}
        public AttemptResult submit(UUID i,String slug,UUID aid,SubmitAnswers body){
            throw new UnsupportedOperationException();
        }
        public Certificate issue(UUID i,String slug){throw new UnsupportedOperationException();}
        public Optional<Certificate> findCertificate(UUID i,String slug){return Optional.empty();}
        public Optional<Certificate> verify(UUID code){return Optional.empty();}
    }
}
