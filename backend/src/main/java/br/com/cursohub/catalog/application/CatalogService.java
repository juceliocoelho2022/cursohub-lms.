package br.com.cursohub.catalog.application;

import br.com.cursohub.catalog.application.exception.CourseNotFoundException;
import br.com.cursohub.catalog.application.exception.InvalidCatalogFilterException;
import br.com.cursohub.catalog.application.model.CatalogFilter;
import br.com.cursohub.catalog.application.model.PageResult;
import br.com.cursohub.catalog.application.port.in.CatalogQueryUseCase;
import br.com.cursohub.catalog.application.port.out.CourseReaderPort;
import br.com.cursohub.catalog.domain.Course;
import java.util.Objects;

/** Caso de uso puro: Java 21, sem importações de Spring e sem dependência de infraestrutura. */
public final class CatalogService implements CatalogQueryUseCase {
    private final CourseReaderPort courses;

    public CatalogService(CourseReaderPort courses) {
        this.courses = Objects.requireNonNull(courses);
    }

    @Override
    public PageResult<Course> list(CatalogFilter filter) {
        Objects.requireNonNull(filter, "filter");
        if (filter.page() < 0 || filter.size() < 1 || filter.size() > 48)
            throw new InvalidCatalogFilterException("Página deve ser >= 0 e tamanho entre 1 e 48");
        if ((filter.search() != null && filter.search().length() > 120)
                || (filter.category() != null && filter.category().length() > 60))
            throw new InvalidCatalogFilterException("Filtro muito longo");

        CatalogFilter normalized = new CatalogFilter(
                normalize(filter.search()), normalize(filter.category()), filter.level(),
                filter.page(), filter.size());
        return courses.findPublished(normalized);
    }

    @Override
    public Course findPublishedBySlug(String slug) {
        if (slug == null || slug.isBlank()) throw new InvalidCatalogFilterException("Slug inválido");
        return courses.findPublishedBySlug(slug)
                .filter(Course::isPublished)
                .orElseThrow(CourseNotFoundException::new);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
