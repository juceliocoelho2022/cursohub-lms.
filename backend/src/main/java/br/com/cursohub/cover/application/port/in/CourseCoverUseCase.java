package br.com.cursohub.cover.application.port.in;

import java.util.Optional;
import java.util.UUID;

public interface CourseCoverUseCase {
    record Cover(String mediaType, byte[] content) {}
    String upload(UUID instructorId, UUID courseId, String filename, byte[] bytes);
    Optional<Cover> forInstructor(UUID instructorId, UUID courseId);
    Optional<Cover> forPublishedCourse(String slug);
}
