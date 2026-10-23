package br.com.cursohub.catalog.adapters.in.web;

import br.com.cursohub.catalog.domain.Course;
import br.com.cursohub.catalog.domain.CourseLevel;
import java.util.UUID;

public record CourseResponse(UUID id, String slug, String title, String description,
                             String category, CourseLevel level, int estimatedMinutes,
                             String thumbnailUrl, int priceCents) {
    public static CourseResponse from(Course course) {
        return new CourseResponse(course.id(), course.slug(), course.title(), course.description(),
                course.category(), course.level(), course.estimatedMinutes(), course.thumbnailUrl(), course.priceCents());
    }
}
