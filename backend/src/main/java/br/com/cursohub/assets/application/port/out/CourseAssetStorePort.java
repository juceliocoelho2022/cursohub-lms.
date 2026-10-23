package br.com.cursohub.assets.application.port.out;

import br.com.cursohub.assets.application.port.in.CourseAssetUseCase.AssetBytes;
import br.com.cursohub.assets.domain.CourseAsset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseAssetStorePort {
    CourseAsset saveOwned(UUID instructor, UUID course, UUID lesson, String name, String type, byte[] data);
    List<CourseAsset> listOwned(UUID instructor, UUID course);
    List<CourseAsset> listEnrolled(UUID student, String slug);
    Optional<AssetBytes> readOwned(UUID instructor, UUID course, UUID asset);
    Optional<AssetBytes> readEnrolled(UUID student, String slug, UUID asset);
}
