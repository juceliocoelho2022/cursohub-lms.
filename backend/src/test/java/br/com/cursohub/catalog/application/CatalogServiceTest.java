package br.com.cursohub.catalog.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.cursohub.catalog.application.exception.CourseNotFoundException;
import br.com.cursohub.catalog.application.exception.InvalidCatalogFilterException;
import br.com.cursohub.catalog.application.model.CatalogFilter;
import br.com.cursohub.catalog.application.model.PageResult;
import br.com.cursohub.catalog.application.port.out.CourseReaderPort;
import br.com.cursohub.catalog.domain.Course;
import br.com.cursohub.catalog.domain.CourseLevel;
import br.com.cursohub.catalog.domain.CourseStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Testes do caso de uso sem banco de dados nem contexto Spring. */
class CatalogServiceTest {
    private FakeCourseRepository repository;
    private CatalogService service;

    @BeforeEach
    void setup() {
        repository = new FakeCourseRepository();
        service = new CatalogService(repository);
    }

    @Test
    void consultaCursosPublicadosComPortaFalsa() {
        repository.courses = List.of(publishedCourse());
        var result = service.list(new CatalogFilter(" JAVA ", " Programação ", CourseLevel.INICIANTE, 0, 12));
        assertEquals(1, result.totalElements());
        assertEquals("java-do-zero", result.content().getFirst().slug());
        assertEquals("java", repository.lastFilter.search());
        assertEquals("programação", repository.lastFilter.category());
    }

    @Test
    void rejeitaPaginacaoInvalidaSemBanco() {
        assertThrows(InvalidCatalogFilterException.class,
                () -> service.list(new CatalogFilter("", "", null, 0, 1000)));
    }

    @Test
    void rascunhoNaoEhVisivelMesmoQueAdaptadorRetorne() {
        Course rascunho = new Course(UUID.randomUUID(), "rascunho", "Rascunho", "d", "c",
                CourseLevel.INICIANTE, CourseStatus.RASCUNHO, UUID.randomUUID(), 10, null, null);
        repository.courses = List.of(rascunho);
        assertThrows(CourseNotFoundException.class, () -> service.findPublishedBySlug("rascunho"));
    }

    @Test
    void dominioNaoPermitePublicarSemData() {
        assertThrows(IllegalArgumentException.class, () -> new Course(UUID.randomUUID(), "java", "Java",
                "d", "c", CourseLevel.INICIANTE, CourseStatus.PUBLICADO, UUID.randomUUID(), 10, null, null));
    }

    @Test
    void retornaCursoPublicado() {
        repository.courses = List.of(publishedCourse());
        assertTrue(service.findPublishedBySlug("java-do-zero").isPublished());
    }

    private static Course publishedCourse() {
        return new Course(UUID.randomUUID(), "java-do-zero", "Java do Zero",
                "Comece com Java", "Programação", CourseLevel.INICIANTE,
                CourseStatus.PUBLICADO, UUID.randomUUID(), 240, null, Instant.parse("2026-01-01T00:00:00Z"));
    }

    static class FakeCourseRepository implements CourseReaderPort {
        List<Course> courses = List.of();
        CatalogFilter lastFilter;

        @Override
        public PageResult<Course> findPublished(CatalogFilter filter) {
            lastFilter = filter;
            List<Course> published = courses.stream().filter(Course::isPublished).toList();
            return new PageResult<>(published, published.size(), filter.page(), filter.size(), 1);
        }

        @Override
        public Optional<Course> findPublishedBySlug(String slug) {
            return courses.stream().filter(c -> c.slug().equals(slug)).findFirst();
        }
    }
}
