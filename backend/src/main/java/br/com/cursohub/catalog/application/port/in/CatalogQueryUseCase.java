package br.com.cursohub.catalog.application.port.in;

import br.com.cursohub.catalog.application.model.CatalogFilter;
import br.com.cursohub.catalog.application.model.PageResult;
import br.com.cursohub.catalog.domain.Course;

/** Porta de entrada: contrato invocado pelo adaptador HTTP (ou futuramente CLI). */
public interface CatalogQueryUseCase {
    PageResult<Course> list(CatalogFilter filter);
    Course findPublishedBySlug(String slug);
}
