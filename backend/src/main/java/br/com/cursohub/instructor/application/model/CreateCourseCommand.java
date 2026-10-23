package br.com.cursohub.instructor.application.model;

import br.com.cursohub.catalog.domain.CourseLevel;

public record CreateCourseCommand(String slug, String title, String description,
                                  String category, CourseLevel level, int estimatedMinutes, int priceCents) {
    public CreateCourseCommand(String slug,String title,String description,String category,
                               CourseLevel level,int estimatedMinutes) {
        this(slug,title,description,category,level,estimatedMinutes,0);
    }
}
