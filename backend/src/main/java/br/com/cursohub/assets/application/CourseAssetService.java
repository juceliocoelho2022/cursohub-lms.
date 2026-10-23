package br.com.cursohub.assets.application;

import br.com.cursohub.assets.application.port.in.CourseAssetUseCase;
import br.com.cursohub.assets.application.port.out.CourseAssetStorePort;
import br.com.cursohub.assets.domain.CourseAsset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public final class CourseAssetService implements CourseAssetUseCase {
    private final CourseAssetStorePort store;
    public CourseAssetService(CourseAssetStorePort store) { this.store = Objects.requireNonNull(store); }

    public CourseAsset upload(UUID instructor, UUID course, UUID lesson, String name, byte[] data) {
        Objects.requireNonNull(instructor);
        Objects.requireNonNull(course);
        if (data == null || data.length == 0 || data.length > 30 * 1024 * 1024)
            throw new AssetValidationException("Arquivo deve conter até 30 MB");
        if (name == null || name.isBlank()) throw new AssetValidationException("Nome do arquivo obrigatório");
        String safeName = name.replace('\\','/'); safeName = safeName.substring(safeName.lastIndexOf('/') + 1);
        safeName = safeName.replaceAll("[\\p{Cntrl}]", "").strip();
        if (safeName.isEmpty() || safeName.length() > 180) throw new AssetValidationException("Nome de arquivo inválido");
        String ext = safeName.substring(Math.max(0, safeName.lastIndexOf('.') + 1)).toLowerCase(Locale.ROOT);
        String type = switch (ext) {
            case "pdf" -> "application/pdf";
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "webp" -> "image/webp";
            case "zip" -> "application/zip";
            case "mp4" -> "video/mp4";
            case "webm" -> "video/webm";
            default -> throw new AssetValidationException("Aceitos: PDF, PNG, JPG, WEBP, ZIP, MP4, WEBM");
        };
        if (lesson != null && !type.startsWith("video/"))
            throw new AssetValidationException("Aulas de vídeo só aceitam MP4 ou WEBM");
        if (lesson == null && type.startsWith("video/"))
            throw new AssetValidationException("Associe vídeos a uma aula VIDEO para acompanhar 100% de exibição");
        boolean signature = switch (type) {
            case "application/pdf" -> starts(data, "%PDF-".getBytes(StandardCharsets.US_ASCII));
            case "image/png" -> starts(data, new byte[]{(byte)137,80,78,71,13,10,26,10});
            case "image/jpeg" -> starts(data, new byte[]{(byte)255,(byte)216,(byte)255});
            case "image/webp" -> data.length > 12 && starts(data, "RIFF".getBytes(StandardCharsets.US_ASCII))
                      && at(data, 8, "WEBP".getBytes(StandardCharsets.US_ASCII));
            case "application/zip" -> starts(data, new byte[]{80,75,3,4});
            case "video/mp4" -> at(data,4,"ftyp".getBytes(StandardCharsets.US_ASCII));
            case "video/webm" -> starts(data, new byte[]{26,69,(byte)223,(byte)163});
            default -> false;
        };
        if (!signature) throw new AssetValidationException("Conteúdo do arquivo não corresponde à extensão");
        return store.saveOwned(instructor,course,lesson,safeName,type,data);
    }
    private static boolean starts(byte[] b, byte[] prefix) { return at(b,0,prefix); }
    private static boolean at(byte[] b,int pos,byte[] prefix) {
        if(b.length<pos+prefix.length) return false;
        for(int i=0;i<prefix.length;i++) if(b[pos+i]!=prefix[i]) return false;
        return true;
    }
    public List<CourseAsset> listInstructor(UUID id,UUID course) { return store.listOwned(id,course); }
    public List<CourseAsset> listStudent(UUID id,String slug) { return store.listEnrolled(id,slug); }
    public AssetBytes downloadInstructor(UUID id,UUID course,UUID asset) {
        return store.readOwned(id,course,asset).orElseThrow(()->new AssetValidationException("Material indisponível"));
    }
    public AssetBytes downloadStudent(UUID id,String slug,UUID asset) {
        return store.readEnrolled(id,slug,asset).orElseThrow(()->new AssetValidationException("Material indisponível"));
    }
}
