package br.com.cursohub.instructor.domain;

import java.util.List;
import java.util.UUID;

public record InstructorModule(UUID id, String title, int position, List<InstructorLesson> lessons) {
    public InstructorModule {
        lessons = List.copyOf(lessons);
    }
}
