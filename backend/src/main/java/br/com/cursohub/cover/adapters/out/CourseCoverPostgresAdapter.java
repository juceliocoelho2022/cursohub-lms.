package br.com.cursohub.cover.adapters.out;

import br.com.cursohub.cover.application.port.in.CourseCoverUseCase.Cover;
import br.com.cursohub.cover.application.port.out.CourseCoverStorePort;
import br.com.cursohub.instructor.application.exception.InstructorCourseNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class CourseCoverPostgresAdapter implements CourseCoverStorePort {
    private final JdbcTemplate jdbc;
    public CourseCoverPostgresAdapter(JdbcTemplate jdbc) { this.jdbc=jdbc; }

    @Override
    @Transactional
    public String saveOwned(UUID instructor,UUID course,String mime,byte[] data) {
        List<String> slugs=jdbc.query("""
            SELECT slug FROM courses WHERE id=? AND instructor_id=? FOR UPDATE
            """,(rs,i)->rs.getString("slug"),course,instructor);
        if(slugs.isEmpty()) throw new InstructorCourseNotFoundException();
        jdbc.update("""
            INSERT INTO course_cover_images(course_id,media_type,size_bytes,content,updated_at)
            VALUES (?,?,?,?,now())
            ON CONFLICT(course_id) DO UPDATE SET
             media_type=excluded.media_type,size_bytes=excluded.size_bytes,
             content=excluded.content,updated_at=now()
            """,course,mime,data.length,data);
        String url="/api/v1/courses/"+slugs.getFirst()+"/cover";
        jdbc.update("UPDATE courses SET thumbnail_url=?,updated_at=now() WHERE id=?",url,course);
        return url;
    }
    @Override
    @Transactional(readOnly=true)
    public Optional<Cover> findOwned(UUID instructor,UUID course) {
        var rows=jdbc.query("""
            SELECT img.media_type,img.content
            FROM course_cover_images img JOIN courses c ON c.id=img.course_id
            WHERE c.id=? AND c.instructor_id=?
            """,(rs,i)->new Cover(rs.getString("media_type"),rs.getBytes("content")),course,instructor);
        return rows.stream().findFirst();
    }
    @Override
    @Transactional(readOnly=true)
    public Optional<Cover> findPublished(String slug) {
        var rows=jdbc.query("""
            SELECT img.media_type,img.content
            FROM course_cover_images img JOIN courses c ON c.id=img.course_id
            WHERE c.slug=? AND c.status='PUBLICADO'
            """,(rs,i)->new Cover(rs.getString("media_type"),rs.getBytes("content")),slug);
        return rows.stream().findFirst();
    }
}
