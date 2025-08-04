package modules.catalog.usecases;

import modules.catalog.core.domain.Book;
import modules.catalog.core.domain.DomainPage;
import modules.catalog.core.usecases.BookServiceImpl;
import modules.catalog.core.usecases.repositories.BookRepository;
import modules.catalog.utils.CatalogTestUtils;
import modules.catalog.web.dto.BookRequestDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateBookSuccessful() {
        BookRequestDTO requestDTO = CatalogTestUtils.createValidBookRequestDTO();
        ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);
        when(bookRepository.save(bookCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        Book createdBook = bookService.createBook(requestDTO);

        assertNotNull(createdBook, "Il libro creato non dovrebbe essere nullo");
        assertNotNull(createdBook.getBookId(), "Il service dovrebbe aver generato un ID");
        assertEquals(requestDTO.getTitle(), createdBook.getTitle());
        assertEquals(requestDTO.getIsbn(), createdBook.getIsbn());
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void testGetBookByIdSuccessful() {
        UUID bookId = UUID.randomUUID();
        Book existingBook = CatalogTestUtils.createValidBookWithId(bookId);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));

        Optional<Book> retrievedBookOpt = bookService.getBookById(bookId);

        assertTrue(retrievedBookOpt.isPresent(),
                "getBookById should return Optional.of(Book) if book exists");
        Book retrievedBook = retrievedBookOpt.get();
        assertEquals(bookId, retrievedBook.getBookId(),
                "Retrieved book should have the correct ID");
        verify(bookRepository, times(1)).findById(bookId);
    }

    @Test
    void testGetBookByIdFails() {
        UUID bookId = UUID.randomUUID();
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        Optional<Book> retrievedBookOpt = bookService.getBookById(bookId);

        assertFalse(retrievedBookOpt.isPresent(),
                "getBookById should return Optional.empty() if book does not exist");
        verify(bookRepository, times(1)).findById(bookId);
    }

    @Test
    void testGetAllBooksSuccessful() {
        List<Book> expectedBooks = Arrays.asList(CatalogTestUtils.createValidBook(),
                CatalogTestUtils.createValidBook());
        when(bookRepository.findAll(null, null, null)).thenReturn(expectedBooks);

        List<Book> retrievedBooks = bookService.getAllBooks(null, null, null);

        assertNotNull(retrievedBooks, "getAllBooks should return a list");
        assertFalse(retrievedBooks.isEmpty(), "List of books should not be empty when books exist");
        assertEquals(expectedBooks.size(), retrievedBooks.size(),
                "List should contain the expected number of books");
        verify(bookRepository, times(1)).findAll(null, null, null);
    }

    @Test
    void testGetAllBooksFails() {
        when(bookRepository.findAll(null, null, null)).thenReturn(new ArrayList<>());

        List<Book> retrievedBooks = bookService.getAllBooks(null, null, null);

        assertNotNull(retrievedBooks, "getAllBooks should return a list even if no books exist");
        assertTrue(retrievedBooks.isEmpty(), "List of books should be empty when no books exist");
        verify(bookRepository, times(1)).findAll(null, null, null);
    }

    @Test
    void testUpdateBookSuccessful() {
        Book bookToUpdate = CatalogTestUtils.createValidBook();
        when(bookRepository.save(any(Book.class))).thenReturn(bookToUpdate);

        Book updatedBook = bookService.updateBook(bookToUpdate);

        assertNotNull(updatedBook, "updateBook should return a Book object");
        assertEquals(bookToUpdate.getBookId(), updatedBook.getBookId(),
                "Returned book should have the same ID as the input book");
        verify(bookRepository, times(1)).save(bookToUpdate);
    }

    @Test
    void testDeleteBookByIdSuccessful() {
        UUID bookIdToDelete = UUID.randomUUID();
        doNothing().when(bookRepository).deleteById(bookIdToDelete);

        bookService.deleteBookById(bookIdToDelete);

        verify(bookRepository, times(1)).deleteById(bookIdToDelete);
    }

    @Test
    @DisplayName("Should search books by delegating to repository")
    void testSearchBooksDelegation() {
        String query = "test query";
        int page = 0;
        int size = 10;
        String sortBy = "title";
        String sortOrder = "asc";

        List<Book> mockContent = Arrays.asList(
                CatalogTestUtils.createTestBook("Test Book 1", "Description 1"),
                CatalogTestUtils.createTestBook("Another Test Book", "Description 2"));
        DomainPage<Book> mockDomainPage = new DomainPage<>(mockContent, 2, 1, page, size, true, true);

        when(bookRepository.searchBooks(anyString(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(mockDomainPage);

        DomainPage<Book> resultPage = bookService.searchBooks(query, page, size, sortBy, sortOrder);

        verify(bookRepository, times(1)).searchBooks(query, page, size, sortBy, sortOrder);

        assertNotNull(resultPage, "Result page should not be null");
        assertEquals(mockDomainPage.totalElements(), resultPage.totalElements(), "Total elements should match");
        assertEquals(mockDomainPage.content().size(), resultPage.content().size(), "Content size should match");
        assertEquals(mockDomainPage.content().get(0).getTitle(), resultPage.content().get(0).getTitle(),
                "First book title should match");
    }
}
