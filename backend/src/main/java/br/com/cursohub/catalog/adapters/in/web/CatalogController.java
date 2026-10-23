package br.com.cursohub.catalog.adapters.in.web;

import br.com.cursohub.catalog.application.model.CatalogFilter;
import br.com.cursohub.catalog.application.port.in.CatalogQueryUseCase;
import br.com.cursohub.catalog.domain.CourseLevel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Adaptador de entrada: conhece HTTP, mas somente a porta do caso de uso. */
@RestController
@RequestMapping("/api/v1/courses")
public class CatalogController {
    private final CatalogQueryUseCase query;

    public CatalogController(CatalogQueryUseCase query) { this.query = query; }

    @GetMapping
    public CatalogPage list(@RequestParam(defaultValue = "") String q,
                            @RequestParam(required = false) String categoria,
                            @RequestParam(required = false) CourseLevel nivel,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "12") int size) {
        var result = query.list(new CatalogFilter(q, categoria, nivel, page, size));
        return CatalogPage.from(result.map(CourseResponse::from));
    }

    @GetMapping("/{slug}")
    public CourseResponse findBySlug(@PathVariable String slug) {
        return CourseResponse.from(query.findPublishedBySlug(slug));
    }
}
