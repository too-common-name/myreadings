package org.modular.playground.catalog.core.domain;

import java.util.List;
import java.util.stream.Collectors;

public record DomainPage<T>(
    List<T> content,
    long totalElements,
    int totalPages,
    int pageNumber,
    int pageSize,
    boolean isLast,
    boolean isFirst
) {
    public static <T> DomainPage<T> of(List<T> allItems, int page, int size) {
        long totalElements = allItems.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int pageNumber = Math.min(page, totalPages > 0 ? totalPages - 1 : 0);

        List<T> pagedContent = allItems.stream()
                .skip((long) pageNumber * size)
                .limit(size)
                .collect(Collectors.toList());

        boolean isFirst = pageNumber == 0;
        boolean isLast = pageNumber >= totalPages - 1;

        return new DomainPage<>(pagedContent, totalElements, totalPages, pageNumber, size, isLast, isFirst);
    }
}