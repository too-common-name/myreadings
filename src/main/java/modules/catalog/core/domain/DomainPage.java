package modules.catalog.core.domain;

import java.util.List;

public record DomainPage<T>(
                List<T> content,
                long totalElements,
                int totalPages,
                int pageNumber,
                int pageSize,
                boolean isLast,
                boolean isFirst) {
}
