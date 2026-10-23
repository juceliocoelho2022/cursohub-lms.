package br.com.cursohub.cover.application;

import br.com.cursohub.cover.application.port.in.CourseCoverUseCase;
import br.com.cursohub.cover.application.port.out.CourseCoverStorePort;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class CourseCoverService implements CourseCoverUseCase {
    public static final int MAX_BYTES = 5 * 1024 * 1024;
    private final CourseCoverStorePort store;
    public CourseCoverService(CourseCoverStorePort store) { this.store=Objects.requireNonNull(store); }

    @Override
    public String upload(UUID instructor, UUID course, String filename, byte[] bytes) {
        Objects.requireNonNull(instructor);
        Objects.requireNonNull(course);
        if(bytes==null || bytes.length==0 || bytes.length>MAX_BYTES)
            throw new CoverValidationException("A imagem deve ter até 5 MB");
        if(filename==null || filename.isBlank())
            throw new CoverValidationException("Informe uma imagem JPG, PNG ou WEBP");
        String lower=filename.toLowerCase(Locale.ROOT);
        String mediaType;
        if(lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            mediaType="image/jpeg";
            if(bytes.length<3 || (bytes[0]&255)!=255 || (bytes[1]&255)!=216 || (bytes[2]&255)!=255)
                throw new CoverValidationException("Arquivo JPEG inválido");
        } else if(lower.endsWith(".png")) {
            mediaType="image/png";
            byte[] sig={(byte)137,80,78,71,13,10,26,10};
            if(!at(bytes,0,sig)) throw new CoverValidationException("Arquivo PNG inválido");
        } else if(lower.endsWith(".webp")) {
            mediaType="image/webp";
            if(!at(bytes,0,"RIFF".getBytes(StandardCharsets.US_ASCII)) ||
                    !at(bytes,8,"WEBP".getBytes(StandardCharsets.US_ASCII)))
                throw new CoverValidationException("Arquivo WEBP inválido");
        } else throw new CoverValidationException("Somente imagens JPG, PNG ou WEBP");
        return store.saveOwned(instructor,course,mediaType,bytes);
    }
    private static boolean at(byte[] bytes,int offset,byte[] prefix) {
        if(bytes.length<offset+prefix.length)return false;
        for(int i=0;i<prefix.length;i++) if(bytes[offset+i]!=prefix[i])return false;
        return true;
    }
    @Override public Optional<Cover> forInstructor(UUID instructor,UUID course) {
        return store.findOwned(Objects.requireNonNull(instructor),Objects.requireNonNull(course));
    }
    @Override public Optional<Cover> forPublishedCourse(String slug) {
        if(slug==null || slug.length()>160 || !slug.matches("[a-z0-9]+(-[a-z0-9]+)*"))
            return Optional.empty();
        return store.findPublished(slug);
    }
}
