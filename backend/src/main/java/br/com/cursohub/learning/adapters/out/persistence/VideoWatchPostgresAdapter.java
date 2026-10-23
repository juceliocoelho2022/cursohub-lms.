package br.com.cursohub.learning.adapters.out.persistence;

import br.com.cursohub.learning.application.exception.LessonUnavailableException;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authoritative sequential server progress for uploaded videos.
 * Reports cannot jump in playback position or exceed real server elapsed time.
 * A determined client may still simulate real-time viewing: this is not a DRM guarantee.
 */
@Repository
public class VideoWatchPostgresAdapter {
    private final JdbcTemplate jdbc;
    public VideoWatchPostgresAdapter(JdbcTemplate jdbc) { this.jdbc=jdbc; }

    public record WatchResponse(int watchedSeconds,int totalSeconds,int percent,boolean completed) {}
    private record Row(UUID enrollment,int duration) {}
    private record Progress(int seconds,Instant last) {}

    @Transactional
    public WatchResponse heartbeat(UUID student,String slug,UUID lesson,int targetSeconds) {
        // Serialize heartbeats per enrollment, and reject unauthorized content.
        var matches=jdbc.query("""
           SELECT e.id AS enrollment_id,l.duration_seconds
             FROM enrollments e
             JOIN courses c ON c.id=e.course_id
             JOIN course_modules m ON m.course_id=c.id
             JOIN lessons l ON l.module_id=m.id
             JOIN course_assets a ON a.lesson_id=l.id AND a.media_type LIKE 'video/%'
            WHERE e.student_id=? AND e.status='ACTIVE' AND c.slug=?
              AND l.id=? AND l.kind='VIDEO' AND l.duration_seconds>0
              AND NOT EXISTS (
                SELECT 1 FROM lessons prior JOIN course_modules pm ON pm.id=prior.module_id
                LEFT JOIN lesson_progress lp ON lp.lesson_id=prior.id AND lp.enrollment_id=e.id
                WHERE pm.course_id=c.id AND (prior.is_required=TRUE OR prior.kind='VIDEO') AND lp.id IS NULL
                AND (pm.position<m.position OR (pm.position=m.position AND prior.position<l.position))
              )
            FOR UPDATE OF e
            """,(rs,i)->new Row(rs.getObject("enrollment_id",UUID.class),rs.getInt("duration_seconds")),
            student,slug,lesson);
        if(matches.isEmpty()) throw new LessonUnavailableException();
        Row info=matches.getFirst();
        if(targetSeconds<0 || targetSeconds>info.duration()) throw new IllegalArgumentException("Posição de vídeo inválida");

        // First request establishes a server clock. Does not grant free seconds of credit.
        jdbc.update("""
          INSERT INTO video_playback_progress(enrollment_id,lesson_id,watched_seconds,last_reported_at)
          VALUES (?,?,0,clock_timestamp()) ON CONFLICT(enrollment_id,lesson_id) DO NOTHING
          """,info.enrollment(),lesson);
        Progress current=jdbc.query("""
          SELECT watched_seconds,last_reported_at FROM video_playback_progress
          WHERE enrollment_id=? AND lesson_id=? FOR UPDATE
          """,(rs,i)->new Progress(rs.getInt("watched_seconds"),
                           rs.getTimestamp("last_reported_at").toInstant()),
                           info.enrollment(),lesson).getFirst();
        int seconds=current.seconds();
        if(targetSeconds>seconds) {
            double elapsed=Duration.between(current.last(),Instant.now()).toMillis()/1000.0;
            int delta=targetSeconds-seconds;
            // Reports run every ~2 s and cannot skip >3 s or accelerate playback.
            if(delta<=3 && elapsed+0.5>=delta && elapsed>=0) {
                jdbc.update("""
                  UPDATE video_playback_progress
                  SET watched_seconds=?,last_reported_at=clock_timestamp()
                  WHERE enrollment_id=? AND lesson_id=?
                  """,targetSeconds,info.enrollment(),lesson);
                seconds=targetSeconds;
            }
        }
        boolean completed=seconds>=info.duration();
        if(completed) jdbc.update("""
           INSERT INTO lesson_progress(enrollment_id,lesson_id) VALUES (?,?)
           ON CONFLICT(enrollment_id,lesson_id) DO NOTHING
           """,info.enrollment(),lesson);
        return new WatchResponse(seconds,info.duration(),
                (int)Math.round(seconds*100.0/info.duration()),completed);
    }
}
