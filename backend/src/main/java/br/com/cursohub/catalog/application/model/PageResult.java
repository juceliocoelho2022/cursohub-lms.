package br.com.cursohub.catalog.application.model;

import java.util.List;
import java.util.function.Function;

public record PageResult<T>(List<T> content, long totalElements, int page, int size, int totalPages) {
    public PageResult {
        content = List.copyOf(content);
    }

    public <R> PageResult<R> map(Function<T, R> mapper) {
        return new PageResult<>(content.stream().map(mapper).toList(), totalElements, page, size, totalPages);
    }
}
