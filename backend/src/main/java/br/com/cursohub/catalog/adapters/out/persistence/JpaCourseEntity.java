package br.com.cursohub.catalog.adapters.out.persistence;

import br.com.cursohub.catalog.domain.Course;
import br.com.cursohub.catalog.domain.CourseLevel;
import br.com.cursohub.catalog.domain.CourseStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Modelo do banco: isolado da entidade de domínio. */
@Entity
@Table(name = "courses")
public class JpaCourseEntity {
    @Id
    private UUID id;
    @Column(nullable = false, length = 160)
    private String slug;
    @Column(nullable = false, length = 180)
    private String title;
    @Column(nullable = false, columnDefinition = "text")
    private String description;
    @Column(nullable = false, length = 60)
    private String category;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CourseLevel level;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CourseStatus status;
    @Column(name = "instructor_id", nullable = false)
    private UUID instructorId;
    @Column(name = "estimated_minutes", nullable = false)
    private int estimatedMinutes;
    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;
    @Column(name = "price_cents", nullable = false)
    private int priceCents;
    @Column(name = "published_at")
    private Instant publishedAt;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected JpaCourseEntity() {}

    public Course toDomain() {
        return new Course(id, slug, title, description, category, level, status,
                instructorId, estimatedMinutes, thumbnailUrl, publishedAt, priceCents);
    }
}
