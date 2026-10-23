package br.com.cursohub.catalog.application.port.out;

import br.com.cursohub.catalog.application.model.CatalogFilter;
import br.com.cursohub.catalog.application.model.PageResult;
import br.com.cursohub.catalog.domain.Course;
import java.util.Optional;

/** Porta de saída: o caso de uso não conhece JPA nem PostgreSQL. */
public interface CourseReaderPort {
    PageResult<Course> findPublished(CatalogFilter filter);
    Optional<Course> findPublishedBySlug(String slug);
}
