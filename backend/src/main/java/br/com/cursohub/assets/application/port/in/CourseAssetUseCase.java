package br.com.cursohub.assets.application.port.in;

import br.com.cursohub.assets.domain.CourseAsset;
import java.util.List;
import java.util.UUID;

public interface CourseAssetUseCase {
    CourseAsset upload(UUID instructorId, UUID courseId, UUID lessonId, String filename, byte[] contents);
    List<CourseAsset> listInstructor(UUID instructorId, UUID courseId);
    List<CourseAsset> listStudent(UUID studentId, String slug);
    AssetBytes downloadInstructor(UUID instructorId, UUID courseId, UUID assetId);
    AssetBytes downloadStudent(UUID studentId, String slug, UUID assetId);
    record AssetBytes(CourseAsset metadata, byte[] content) {}
}
