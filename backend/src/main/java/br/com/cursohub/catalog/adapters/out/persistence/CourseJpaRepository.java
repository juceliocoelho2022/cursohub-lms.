package br.com.cursohub.catalog.adapters.out.persistence;

import br.com.cursohub.catalog.domain.CourseStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface CourseJpaRepository extends JpaRepository<JpaCourseEntity, UUID>, JpaSpecificationExecutor<JpaCourseEntity> {
    Optional<JpaCourseEntity> findBySlugAndStatus(String slug, CourseStatus status);
}
