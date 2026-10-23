package br.com.cursohub.catalog.adapters.out.persistence;

import br.com.cursohub.catalog.application.model.CatalogFilter;
import br.com.cursohub.catalog.application.model.PageResult;
import br.com.cursohub.catalog.application.port.out.CourseReaderPort;
import br.com.cursohub.catalog.domain.Course;
import br.com.cursohub.catalog.domain.CourseStatus;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Adaptador de saída: converte objetos JPA em objetos puros de domínio. */
@Repository
@Transactional(readOnly = true)
public class CoursePostgresAdapter implements CourseReaderPort {
    private final CourseJpaRepository jpa;

    public CoursePostgresAdapter(CourseJpaRepository jpa) { this.jpa = jpa; }

    @Override
    public PageResult<Course> findPublished(CatalogFilter filter) {
        Specification<JpaCourseEntity> criteria = (root, query, cb) -> {
            List<Predicate> where = new ArrayList<>();
            where.add(cb.equal(root.get("status"), CourseStatus.PUBLICADO));
            if (!filter.search().isBlank()) {
                String pattern = "%" + escapeLike(filter.search()) + "%";
                where.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern, '\\'),
                        cb.like(cb.lower(root.get("description")), pattern, '\\')));
            }
            if (!filter.category().isBlank())
                where.add(cb.equal(cb.lower(root.get("category")), filter.category()));
            if (filter.level() != null)
                where.add(cb.equal(root.get("level"), filter.level()));
            return cb.and(where.toArray(Predicate[]::new));
        };
        var pageable = PageRequest.of(filter.page(), filter.size(),
                Sort.by("title").ascending().and(Sort.by("id")));
        var result = jpa.findAll(criteria, pageable);
        return new PageResult<>(result.getContent().stream().map(JpaCourseEntity::toDomain).toList(),
                result.getTotalElements(), result.getNumber(), result.getSize(), result.getTotalPages());
    }

    @Override
    public Optional<Course> findPublishedBySlug(String slug) {
        return jpa.findBySlugAndStatus(slug, CourseStatus.PUBLICADO).map(JpaCourseEntity::toDomain);
    }

    private static String escapeLike(String input) {
        return input.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
