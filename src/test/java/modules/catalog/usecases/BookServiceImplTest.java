package modules.catalog.usecases;

import modules.catalog.domain.Book;
import modules.catalog.infrastructure.BookRepository;
import modules.catalog.utils.CatalogTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        Book bookToCreate = CatalogTestUtils.createValidBook();
        when(bookRepository.save(any(Book.class))).thenReturn(bookToCreate);

        Book createdBook = bookService.createBook(bookToCreate);

        assertNotNull(createdBook, "createBook should return a Book object");
        assertEquals(bookToCreate.getBookId(), createdBook.getBookId(),
                "Returned book should have the same ID as the input book");
        verify(bookRepository, times(1)).save(bookToCreate);
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
        when(bookRepository.findAll()).thenReturn(expectedBooks);

        List<Book> retrievedBooks = bookService.getAllBooks();

        assertNotNull(retrievedBooks, "getAllBooks should return a list");
        assertFalse(retrievedBooks.isEmpty(), "List of books should not be empty when books exist");
        assertEquals(expectedBooks.size(), retrievedBooks.size(),
                "List should contain the expected number of books");
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    void testGetAllBooksFails() {
        when(bookRepository.findAll()).thenReturn(Collections.emptyList());

        List<Book> retrievedBooks = bookService.getAllBooks();

        assertNotNull(retrievedBooks, "getAllBooks should return a list even if no books exist");
        assertTrue(retrievedBooks.isEmpty(), "List of books should be empty when no books exist");
        verify(bookRepository, times(1)).findAll();
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
}
