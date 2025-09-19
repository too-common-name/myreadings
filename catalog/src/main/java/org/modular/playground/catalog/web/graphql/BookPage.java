package org.modular.playground.catalog.web.graphql;

import org.modular.playground.catalog.core.domain.Book;
import org.modular.playground.catalog.core.domain.DomainPage;
import org.modular.playground.catalog.infrastructure.persistence.postgres.mapper.BookMapper;
import org.modular.playground.catalog.web.dto.BookResponseDTO;

import lombok.Data;

import java.util.List;

@Data
public class BookPage {
    private List<BookResponseDTO> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean isLast;
    private boolean isFirst;

    public static BookPage from(DomainPage<Book> domainPage, BookMapper mapper) {
        BookPage page = new BookPage();
        page.setContent(mapper.toResponseDTOs(domainPage.content()));
        page.setPageNumber(domainPage.pageNumber());
        page.setPageSize(domainPage.pageSize());
        page.setTotalElements(domainPage.totalElements());
        page.setTotalPages(domainPage.totalPages());
        page.setLast(domainPage.isLast());
        page.setFirst(domainPage.isFirst());
        return page;
    }
}