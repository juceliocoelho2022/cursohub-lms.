package br.com.cursohub.catalog.adapters.in.web;

import br.com.cursohub.catalog.application.model.PageResult;
import java.util.List;

public record CatalogPage(List<CourseResponse> content, long totalElements,
                          int page, int size, int totalPages) {
    public static CatalogPage from(PageResult<CourseResponse> result) {
        return new CatalogPage(result.content(), result.totalElements(), result.page(),
                result.size(), result.totalPages());
    }
}
