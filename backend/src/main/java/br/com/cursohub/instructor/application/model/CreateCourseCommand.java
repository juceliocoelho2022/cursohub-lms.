package br.com.cursohub.instructor.application.model;

import br.com.cursohub.catalog.domain.CourseLevel;

public record CreateCourseCommand(String slug, String title, String description,
                                  String category, CourseLevel level, int estimatedMinutes) {}
