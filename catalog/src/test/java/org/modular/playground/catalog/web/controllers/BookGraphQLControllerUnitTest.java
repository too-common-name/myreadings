package org.modular.playground.catalog.web.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modular.playground.catalog.core.domain.Book;
import org.modular.playground.catalog.core.domain.DomainPage;
import org.modular.playground.catalog.core.usecases.BookService;
import org.modular.playground.catalog.infrastructure.persistence.postgres.mapper.BookMapper;
import org.modular.playground.catalog.infrastructure.persistence.postgres.mapper.BookMapperImpl;
import org.modular.playground.catalog.utils.CatalogTestUtils;
import org.modular.playground.catalog.web.dto.BookRequestDTO;
import org.modular.playground.catalog.web.dto.BookResponseDTO;
import org.modular.playground.catalog.web.graphql.BookGraphQLController;
import org.modular.playground.catalog.web.graphql.BookPage;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookGraphQLControllerUnitTest {

    @InjectMocks
    private BookGraphQLController bookGraphQLController;

    @Spy
    private BookMapper bookMapper = new BookMapperImpl();

    @Mock
    private BookService bookService;

    @Test
    void shouldReturnCreatedWhenBookIsCreated() {
        BookRequestDTO bookInput = new BookRequestDTO();
        bookInput.setIsbn("12345");
        Book createdBook = CatalogTestUtils.createValidBook();
        when(bookService.createBook(any(BookRequestDTO.class))).thenReturn(createdBook);

        BookResponseDTO result = bookGraphQLController.createBook(bookInput);

        assertNotNull(result);
        assertEquals(createdBook.getIsbn(), result.getIsbn());
        verify(bookService, times(1)).createBook(bookInput);
    }

    @Test
    void shouldReturnOkWithBookDtoWhenIdExists() {
        UUID bookId = UUID.randomUUID();
        Book mockBook = CatalogTestUtils.createValidBookWithId(bookId);
        when(bookService.getBookById(bookId)).thenReturn(Optional.of(mockBook));

        BookResponseDTO result = bookGraphQLController.getBookById(bookId);

        assertNotNull(result);
        assertEquals(bookId, result.getBookId());
        verify(bookService, times(1)).getBookById(bookId);
    }

    @Test
    void shouldReturnNullWhenIdDoesNotExist() {
        UUID bookId = UUID.randomUUID();
        when(bookService.getBookById(bookId)).thenReturn(Optional.empty());

        BookResponseDTO result = bookGraphQLController.getBookById(bookId);

        assertNull(result);
        verify(bookService, times(1)).getBookById(bookId);
    }

    @Test
    void shouldReturnOkWithBookDtoList() {
        List<Book> mockBooks = Collections.singletonList(CatalogTestUtils.createValidBook());

        when(bookService.getAllBooks(null, null, 25)).thenReturn(mockBooks);

        List<BookResponseDTO> result = bookGraphQLController.getAllBooks(null, null, 25);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(bookService, times(1)).getAllBooks(null, null, 25);
    }

    @Test
    void shouldReturnPagedResponseWhenSearchingBooks() {
        String query = "test";
        int page = 0;
        int size = 10;
        String sortBy = "title";
        String sortOrder = "asc";

        DomainPage<Book> mockDomainPage = new DomainPage<>(
                Collections.singletonList(CatalogTestUtils.createValidBook()),
                1,
                size,
                page,
                1,
                true,
                true
        );
        when(bookService.searchBooks(query, page, size, sortBy, sortOrder)).thenReturn(mockDomainPage);

        BookPage result = bookGraphQLController.searchBooks(query, page, size, sortBy, sortOrder);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(0, result.getPageNumber());
        assertFalse(result.getContent().isEmpty());
        verify(bookService, times(1)).searchBooks(query, page, size, sortBy, sortOrder);
    }
}