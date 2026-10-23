package br.com.cursohub.assets.application;

import static org.junit.jupiter.api.Assertions.*;
import br.com.cursohub.assets.application.port.in.CourseAssetUseCase.AssetBytes;
import br.com.cursohub.assets.application.port.out.CourseAssetStorePort;
import br.com.cursohub.assets.domain.CourseAsset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.jupiter.api.Test;

class CourseAssetServiceTest {
    private final UUID instructor=UUID.randomUUID(),course=UUID.randomUUID(),lesson=UUID.randomUUID();
    private final FakeStore store=new FakeStore();
    private final CourseAssetService service=new CourseAssetService(store);
    @Test void aceitaPdfRealAssinado() {
        var result=service.upload(instructor,course,null,"../material.pdf",
          "%PDF-1.7\nconteúdo".getBytes(StandardCharsets.UTF_8));
        assertEquals("material.pdf",result.filename());
        assertEquals("application/pdf",result.mediaType());
    }
    @Test void recusaExtensaoFalsa() {
        assertThrows(AssetValidationException.class,()->service.upload(instructor,course,null,
          "arquivo.pdf","isso não é PDF".getBytes(StandardCharsets.UTF_8)));
    }
    @Test void recusaZipComoVideo() {
        assertThrows(AssetValidationException.class,()->service.upload(instructor,course,lesson,
          "material.zip",new byte[]{80,75,3,4,0,0}));
    }
    @Test void aceitaVideoMp4VinculadoAAula() {
        byte[] fakeHeader={0,0,0,24,102,116,121,112,105,115,111,109};
        var result=service.upload(instructor,course,lesson,"introducao.mp4",fakeHeader);
        assertEquals(lesson,result.lessonId());
        assertEquals("video/mp4",result.mediaType());
    }
    @Test void bloqueiaVideoSemAula() {
        assertThrows(AssetValidationException.class,()->service.upload(instructor,course,null,
          "video.mp4",new byte[]{0,0,0,24,102,116,121,112,105,115,111,109}));
    }
    @Test void recusaVaziosEEccessivos() {
        assertThrows(AssetValidationException.class,()->service.upload(instructor,course,null,"foto.png",new byte[0]));
        assertThrows(AssetValidationException.class,()->service.upload(instructor,course,null,"foto.png",new byte[30*1024*1024+1]));
    }
    class FakeStore implements CourseAssetStorePort {
        public CourseAsset saveOwned(UUID instructor,UUID course,UUID lesson,String filename,String type,byte[] bytes) {
            return new CourseAsset(UUID.randomUUID(),course,lesson,filename,type,bytes.length);
        }
        public List<CourseAsset> listOwned(UUID i,UUID c){return List.of();}
        public List<CourseAsset> listEnrolled(UUID i,String slug){return List.of();}
        public Optional<AssetBytes> readOwned(UUID i,UUID c,UUID asset){return Optional.empty();}
        public Optional<AssetBytes> readEnrolled(UUID i,String slug,UUID asset){return Optional.empty();}
    }
}
