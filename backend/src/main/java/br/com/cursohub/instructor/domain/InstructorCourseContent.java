package br.com.cursohub.instructor.domain;

import java.util.List;

public record InstructorCourseContent(InstructorCourse course, List<InstructorModule> modules) {
    public InstructorCourseContent {
        modules = List.copyOf(modules);
    }
}
