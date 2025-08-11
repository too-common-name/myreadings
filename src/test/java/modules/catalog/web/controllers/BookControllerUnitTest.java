package modules.catalog.web.controllers;

import modules.catalog.core.domain.Book;
import modules.catalog.core.domain.DomainPage;
import modules.catalog.core.usecases.BookService;
import modules.catalog.utils.CatalogTestUtils;
import modules.catalog.web.dto.BookRequestDTO;
import modules.catalog.web.dto.BookResponseDTO;
import modules.catalog.web.dto.PagedResponse;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class BookControllerUnitTest {

    @InjectMocks
    private BookController bookController;

    @Mock
    private BookService bookService;

    @Test
    void createBook_shouldReturnCreated_whenRequestIsValid() {
        BookRequestDTO bookRequest = CatalogTestUtils.createValidBookRequestDTO();
        Book createdBook = CatalogTestUtils.createValidBook(); // Simula il libro creato dal service

        when(bookService.createBook(any(BookRequestDTO.class))).thenReturn(createdBook);

        Response response = bookController.createBook(bookRequest);

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());

        BookResponseDTO responseBody = (BookResponseDTO) response.getEntity();
        assertEquals(createdBook.getBookId(), responseBody.getBookId());
        assertEquals(createdBook.getTitle(), responseBody.getTitle());

        verify(bookService).createBook(bookRequest);
    }
    
    @Test
    void getBookById_shouldReturnOkAndBook_whenBookExists() {
        UUID bookId = UUID.randomUUID();
        Book mockBook = CatalogTestUtils.createValidBookWithId(bookId);

        when(bookService.getBookById(bookId)).thenReturn(Optional.of(mockBook));

        Response response = bookController.getBookById(bookId);
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());

        BookResponseDTO responseBody = (BookResponseDTO) response.getEntity();
        assertEquals(bookId, responseBody.getBookId());

        verify(bookService).getBookById(bookId);
    }

    @Test
    void getBookById_shouldReturnNotFound_whenBookDoesNotExist() {
        UUID bookId = UUID.randomUUID();
        when(bookService.getBookById(bookId)).thenReturn(Optional.empty());

        Response response = bookController.getBookById(bookId);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertNull(response.getEntity());
        verify(bookService).getBookById(bookId);
    }

    
    @Test
    void searchBooks_shouldDelegateToServiceAndReturnPagedResponse() {
        String query = "test";
        int page = 0;
        int size = 10;
        String sortBy = "title";
        String sortOrder = "asc";

        List<Book> mockDomainBooks = Arrays.asList(
            CatalogTestUtils.createTestBook("Test Book A", "Description A"),
            CatalogTestUtils.createTestBook("Another Test Book B", "Description B")
        );
        DomainPage<Book> mockDomainPage = new DomainPage<>(mockDomainBooks, 2, 1, page, size, true, true);

        when(bookService.searchBooks(query, page, size, sortBy, sortOrder)).thenReturn(mockDomainPage);

        PagedResponse<BookResponseDTO> response = bookController.searchBooks(query, page, size, sortBy, sortOrder);

        verify(bookService).searchBooks(query, page, size, sortBy, sortOrder);

        assertNotNull(response);
        assertEquals(2, response.content().size());
        assertEquals(2, response.totalElements());
        assertEquals(1, response.totalPages());
        assertEquals(mockDomainBooks.get(0).getTitle(), response.content().get(0).getTitle());
    }
}