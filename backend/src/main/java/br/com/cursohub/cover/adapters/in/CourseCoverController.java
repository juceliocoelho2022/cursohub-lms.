package br.com.cursohub.cover.adapters.in;

import br.com.cursohub.cover.application.CoverValidationException;
import br.com.cursohub.cover.application.CourseCoverService;
import br.com.cursohub.cover.application.port.in.CourseCoverUseCase;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class CourseCoverController {
    private final CourseCoverUseCase covers;
    public CourseCoverController(CourseCoverUseCase covers) { this.covers=covers; }

    @PostMapping(path="/api/v1/instructor/courses/{courseId}/cover",
                 consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String,String> upload(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID courseId,
                                    @RequestPart("file") MultipartFile file) throws IOException {
        if(file.isEmpty() || file.getSize()>CourseCoverService.MAX_BYTES)
            throw new CoverValidationException("A imagem deve ter até 5 MB");
        String url=covers.upload(UUID.fromString(jwt.getSubject()),courseId,
                                 file.getOriginalFilename(),file.getBytes());
        return Map.of("thumbnailUrl",url);
    }

    @GetMapping("/api/v1/instructor/courses/{courseId}/cover")
    public ResponseEntity<byte[]> instructor(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID courseId) {
        return covers.forInstructor(UUID.fromString(jwt.getSubject()),courseId)
            .map(CourseCoverController::response)
            .orElseGet(()->ResponseEntity.notFound().build());
    }

    @GetMapping("/api/v1/courses/{slug}/cover")
    public ResponseEntity<byte[]> published(@PathVariable String slug) {
        return covers.forPublishedCourse(slug)
            .map(CourseCoverController::response)
            .orElseGet(()->ResponseEntity.notFound().build());
    }

    private static ResponseEntity<byte[]> response(CourseCoverUseCase.Cover image) {
        return ResponseEntity.ok()
          .header("X-Content-Type-Options","nosniff")
          .header(HttpHeaders.CONTENT_DISPOSITION,"inline")
          .cacheControl(CacheControl.noStore())
          .contentType(MediaType.parseMediaType(image.mediaType()))
          .contentLength(image.content().length)
          .body(image.content());
    }
}
