package br.com.cursohub.learning.adapters.in.web;

import br.com.cursohub.learning.adapters.out.persistence.VideoWatchPostgresAdapter;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/students/me/courses/{slug}/lessons/{lessonId}/video-progress")
public class VideoProgressController {
    private final VideoWatchPostgresAdapter watch;
    public VideoProgressController(VideoWatchPostgresAdapter watch) { this.watch=watch; }

    @PostMapping
    public VideoWatchPostgresAdapter.WatchResponse heartbeat(@AuthenticationPrincipal Jwt jwt,
                  @PathVariable String slug,@PathVariable UUID lessonId,@RequestBody WatchRequest request) {
        return watch.heartbeat(UUID.fromString(jwt.getSubject()),slug,lessonId,request.seconds());
    }
    public record WatchRequest(int seconds) {}
}
