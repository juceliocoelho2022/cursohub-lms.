package br.com.cursohub.assets.adapters.in;

import br.com.cursohub.assets.application.AssetValidationException;
import br.com.cursohub.assets.application.port.in.CourseAssetUseCase;
import br.com.cursohub.assets.domain.CourseAsset;
import java.util.List;
import java.util.UUID;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class AssetController {
    private final CourseAssetUseCase assets;
    public AssetController(CourseAssetUseCase assets) { this.assets=assets; }

    @PostMapping(path="/api/v1/instructor/courses/{courseId}/assets",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CourseAsset upload(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID courseId,
                @RequestParam(required=false) UUID lessonId,@RequestPart("file") MultipartFile file) throws java.io.IOException {
        if(file.isEmpty() || file.getSize()>30L*1024*1024) throw new AssetValidationException("Arquivo vazio ou maior que 30 MB");
        return assets.upload(UUID.fromString(jwt.getSubject()),courseId,lessonId,file.getOriginalFilename(),file.getBytes());
    }
    @GetMapping("/api/v1/instructor/courses/{courseId}/assets")
    public List<CourseAsset> listInstructor(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID courseId) {
        return assets.listInstructor(UUID.fromString(jwt.getSubject()),courseId);
    }
    @GetMapping("/api/v1/students/me/courses/{slug}/assets")
    public List<CourseAsset> listStudent(@AuthenticationPrincipal Jwt jwt,@PathVariable String slug) {
        return assets.listStudent(UUID.fromString(jwt.getSubject()),slug);
    }
    @GetMapping("/api/v1/instructor/courses/{courseId}/assets/{assetId}")
    public ResponseEntity<byte[]> downloadInstructor(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID courseId,@PathVariable UUID assetId) {
        return response(assets.downloadInstructor(UUID.fromString(jwt.getSubject()),courseId,assetId));
    }
    @GetMapping("/api/v1/students/me/courses/{slug}/assets/{assetId}")
    public ResponseEntity<byte[]> downloadStudent(@AuthenticationPrincipal Jwt jwt,@PathVariable String slug,@PathVariable UUID assetId) {
        return response(assets.downloadStudent(UUID.fromString(jwt.getSubject()),slug,assetId));
    }
    private ResponseEntity<byte[]> response(CourseAssetUseCase.AssetBytes bytes) {
        var asset=bytes.metadata();
        return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename*=UTF-8''"+
              java.net.URLEncoder.encode(asset.filename(),java.nio.charset.StandardCharsets.UTF_8).replace("+","%20"))
          .header("X-Content-Type-Options","nosniff")
          .header("Cache-Control","no-store")
          .contentType(MediaType.parseMediaType(asset.mediaType()))
          .contentLength(bytes.content().length).body(bytes.content());
    }
}
