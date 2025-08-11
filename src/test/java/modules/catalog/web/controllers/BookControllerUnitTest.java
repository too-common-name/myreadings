package modules.catalog.web.controllers;

import jakarta.ws.rs.core.Response;
import modules.catalog.core.domain.Book;
import modules.catalog.core.domain.DomainPage;
import modules.catalog.core.usecases.BookService;
import modules.catalog.utils.CatalogTestUtils;
import modules.catalog.web.dto.BookRequestDTO;
import modules.catalog.web.dto.BookResponseDTO;
import modules.catalog.web.dto.PagedResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookControllerUnitTest {

    @InjectMocks
    private BookController bookController;

    @Mock
    private BookService bookService;

    @Test
    void shouldReturnCreatedWhenBookIsCreated() {
        BookRequestDTO bookRequest = CatalogTestUtils.createValidBookRequestDTO();
        Book createdBook = CatalogTestUtils.createValidBook();
        when(bookService.createBook(any(BookRequestDTO.class))).thenReturn(createdBook);
        
        Response response = bookController.createBook(bookRequest);
        
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(bookService, times(1)).createBook(bookRequest);
    }

    @Test
    void shouldReturnOkWithBookDtoWhenIdExists() {
        UUID bookId = UUID.randomUUID();
        Book mockBook = CatalogTestUtils.createValidBookWithId(bookId);
        when(bookService.getBookById(bookId)).thenReturn(Optional.of(mockBook));

        Response response = bookController.getBookById(bookId);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(bookService, times(1)).getBookById(bookId);
    }

    @Test
    void shouldReturnNotFoundWhenIdDoesNotExist() {
        UUID bookId = UUID.randomUUID();
        when(bookService.getBookById(bookId)).thenReturn(Optional.empty());
        
        Response response = bookController.getBookById(bookId);
        
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        verify(bookService, times(1)).getBookById(bookId);
    }

    @Test
    void shouldReturnOkWithBookDtoList() {
        List<Book> mockBooks = Collections.singletonList(CatalogTestUtils.createValidBook());
        when(bookService.getAllBooks(null, null, null)).thenReturn(mockBooks);
        
        Response response = bookController.getAllBooks(null, null, null);
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(bookService, times(1)).getAllBooks(null, null, null);
    }

    @Test
    void shouldReturnPagedResponseWhenSearchingBooks() {
        String query = "test";
        DomainPage<Book> mockDomainPage = new DomainPage<>(Collections.emptyList(), 0, 0, 0, 10, true, true);
        when(bookService.searchBooks(query, 0, 10, "title", "asc")).thenReturn(mockDomainPage);

        PagedResponse<BookResponseDTO> response = bookController.searchBooks(query, 0, 10, "title", "asc");

        assertNotNull(response);
        verify(bookService, times(1)).searchBooks(query, 0, 10, "title", "asc");
    }
}