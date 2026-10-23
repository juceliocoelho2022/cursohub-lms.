package br.com.cursohub.cover.application;

import static org.junit.jupiter.api.Assertions.*;
import br.com.cursohub.cover.application.port.in.CourseCoverUseCase.Cover;
import br.com.cursohub.cover.application.port.out.CourseCoverStorePort;
import java.util.*;
import org.junit.jupiter.api.Test;

class CourseCoverServiceTest {
    private final UUID instructor=UUID.randomUUID(),course=UUID.randomUUID();
    private final StubStore store=new StubStore();
    private final CourseCoverService service=new CourseCoverService(store);
    @Test void aceitaJpegRealComCabecalhoEIdentidadeDoInstrutor(){
        assertEquals("/api/v1/courses/curso-java/cover",service.upload(instructor,course,
                 "java-basico.jpg",new byte[]{(byte)255,(byte)216,(byte)255,1,2,3}));
        assertEquals(instructor,store.instructor);
        assertEquals(course,store.course);
        assertEquals("image/jpeg",store.mime);
    }
    @Test void aceitaPngEWebpValidos(){
        service.upload(instructor,course,"capa.png",new byte[]{(byte)137,80,78,71,13,10,26,10,1});
        assertEquals("image/png",store.mime);
        service.upload(instructor,course,"capa.webp",new byte[]{82,73,70,70,0,0,0,0,87,69,66,80,1});
        assertEquals("image/webp",store.mime);
    }
    @Test void rejeitaAssinaturaFalsaEFormatosAtivos(){
        assertThrows(CoverValidationException.class,()->service.upload(instructor,course,
                      "capa.png",new byte[]{1,2,3}));
        assertThrows(CoverValidationException.class,()->service.upload(instructor,course,
                      "imagem.svg","<svg/>".getBytes()));
        assertThrows(CoverValidationException.class,()->service.upload(instructor,course,
                      "capa.jpg",new byte[0]));
    }
    @Test void bloqueiaImagensMaioresQueCincoMb(){
        assertThrows(CoverValidationException.class,()->service.upload(instructor,course,
                "capa.png",new byte[CourseCoverService.MAX_BYTES+1]));
    }
    @Test void naoConsultaSlugArbitrario(){
        assertTrue(service.forPublishedCourse("../../admin").isEmpty());
    }
    private static final class StubStore implements CourseCoverStorePort {
        UUID instructor,course;String mime;
        public String saveOwned(UUID i,UUID c,String m,byte[] data) {
            instructor=i;course=c;mime=m;
            return "/api/v1/courses/curso-java/cover";
        }
        public Optional<Cover> findOwned(UUID i,UUID c) {return Optional.empty();}
        public Optional<Cover> findPublished(String slug){return Optional.empty();}
    }
}
