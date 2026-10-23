package br.com.cursohub.cover.application.port.out;

import br.com.cursohub.cover.application.port.in.CourseCoverUseCase.Cover;
import java.util.Optional;
import java.util.UUID;

public interface CourseCoverStorePort {
    String saveOwned(UUID instructor,UUID course,String mime,byte[] data);
    Optional<Cover> findOwned(UUID instructor, UUID course);
    Optional<Cover> findPublished(String slug);
}
