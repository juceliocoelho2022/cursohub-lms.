package br.com.cursohub.assets.adapters.out;

import br.com.cursohub.assets.application.AssetValidationException;
import br.com.cursohub.assets.application.port.in.CourseAssetUseCase.AssetBytes;
import br.com.cursohub.assets.application.port.out.CourseAssetStorePort;
import br.com.cursohub.assets.domain.CourseAsset;
import java.sql.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class AssetPostgresAdapter implements CourseAssetStorePort {
    private final JdbcTemplate jdbc;
    public AssetPostgresAdapter(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private static CourseAsset map(ResultSet rs) throws SQLException {
        return new CourseAsset(rs.getObject("id",UUID.class),rs.getObject("course_id",UUID.class),
            rs.getObject("lesson_id",UUID.class), rs.getString("filename"),
            rs.getString("media_type"),rs.getLong("size_bytes"));
    }
    private static final String COLS = "a.id,a.course_id,a.lesson_id,a.filename,a.media_type,a.size_bytes";

    @Override
    @Transactional
    public CourseAsset saveOwned(UUID instructor,UUID course,UUID lesson,String name,String type,byte[] contents) {
        var found=jdbc.query("SELECT id FROM courses WHERE id=? AND instructor_id=?",
            (rs,i)->rs.getObject(1,UUID.class),course,instructor);
        if(found.isEmpty()) throw new AssetValidationException("Curso não pertence ao instrutor");
        if(lesson!=null) {
            var rows=jdbc.query("""
                SELECT l.id FROM lessons l JOIN course_modules m ON m.id=l.module_id
                WHERE l.id=? AND m.course_id=? AND l.kind='VIDEO'
                """,(rs,i)->rs.getObject(1,UUID.class),lesson,course);
            if(rows.isEmpty()) throw new AssetValidationException("Aula VIDEO não pertence a este curso");
            Long alreadyLinked=jdbc.queryForObject("SELECT COUNT(*) FROM course_assets WHERE lesson_id=?",Long.class,lesson);
            if(alreadyLinked!=null && alreadyLinked>0)
                throw new AssetValidationException("Vídeo já enviado: para substituir, crie uma nova versão da aula");
        }
        UUID id=UUID.randomUUID();
        jdbc.update("""
            INSERT INTO course_assets(id,course_id,lesson_id,filename,media_type,size_bytes,contents)
            VALUES (?,?,?,?,?,?,?)
            """,id,course,lesson,name,type,contents.length,contents);
        return new CourseAsset(id,course,lesson,name,type,contents.length);
    }

    @Override
    @Transactional(readOnly=true)
    public List<CourseAsset> listOwned(UUID instructor,UUID course) {
        return jdbc.query("SELECT "+COLS+" FROM course_assets a JOIN courses c ON c.id=a.course_id WHERE c.instructor_id=? AND c.id=? ORDER BY a.created_at,a.id",
            (rs,i)->map(rs),instructor,course);
    }
    @Override
    @Transactional(readOnly=true)
    public List<CourseAsset> listEnrolled(UUID student,String slug) {
        return jdbc.query("SELECT "+COLS+" "+"""
            FROM course_assets a
            JOIN courses c ON c.id=a.course_id
            JOIN enrollments e ON e.course_id=c.id
            WHERE e.student_id=? AND e.status IN ('ACTIVE','COMPLETED') AND c.slug=?
            ORDER BY a.created_at,a.id
            """,(rs,i)->map(rs),student,slug);
    }
    @Override
    @Transactional(readOnly=true)
    public Optional<AssetBytes> readOwned(UUID instructor,UUID course,UUID asset) {
        var rows=jdbc.query("SELECT "+COLS+",a.contents FROM course_assets a JOIN courses c ON c.id=a.course_id WHERE c.id=? AND c.instructor_id=? AND a.id=?",
            (rs,i)->new AssetBytes(map(rs),rs.getBytes("contents")),course,instructor,asset);
        return rows.stream().findFirst();
    }
    @Override
    @Transactional(readOnly=true)
    public Optional<AssetBytes> readEnrolled(UUID student,String slug,UUID asset) {
        // Uploaded video of a locked lesson is not delivered by direct asset URL.
        var rows=jdbc.query("SELECT "+COLS+",a.contents FROM course_assets a "+
            "JOIN courses c ON c.id=a.course_id "+
            "JOIN enrollments e ON e.course_id=c.id "+
            "LEFT JOIN lessons target ON target.id=a.lesson_id "+
            "LEFT JOIN course_modules target_m ON target_m.id=target.module_id "+
            "WHERE e.student_id=? AND e.status IN ('ACTIVE','COMPLETED') AND c.slug=? AND a.id=? "+
            "AND (a.lesson_id IS NULL OR NOT EXISTS ("+
            " SELECT 1 FROM lessons prev JOIN course_modules pm ON pm.id=prev.module_id "+
            " LEFT JOIN lesson_progress lp ON lp.lesson_id=prev.id AND lp.enrollment_id=e.id "+
            " WHERE pm.course_id=c.id AND (prev.is_required=TRUE OR prev.kind='VIDEO') AND lp.id IS NULL "+
            " AND (pm.position<target_m.position OR (pm.position=target_m.position AND prev.position<target.position))"+
            "))",
            (rs,i)->new AssetBytes(map(rs),rs.getBytes("contents")),student,slug,asset);
        return rows.stream().findFirst();
    }
}
