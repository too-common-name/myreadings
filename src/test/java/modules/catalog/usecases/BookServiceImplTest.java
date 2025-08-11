package modules.catalog.usecases;

import modules.catalog.core.domain.Book;
import modules.catalog.core.domain.DomainPage;
import modules.catalog.core.usecases.BookServiceImpl;
import modules.catalog.core.usecases.repositories.BookRepository;
import modules.catalog.utils.CatalogTestUtils;
import modules.catalog.web.dto.BookRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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
    void shouldCreateBookWhenRequestIsValid() {
        BookRequestDTO requestDTO = CatalogTestUtils.createValidBookRequestDTO();
        ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);
        when(bookRepository.save(bookCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        
        Book createdBook = bookService.createBook(requestDTO);

        assertNotNull(createdBook);
        assertNotNull(createdBook.getBookId());
        assertEquals(requestDTO.getTitle(), createdBook.getTitle());
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void shouldReturnBookWhenIdExists() {
        UUID bookId = UUID.randomUUID();
        Book existingBook = CatalogTestUtils.createValidBookWithId(bookId);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));

        Optional<Book> retrievedBookOpt = bookService.getBookById(bookId);

        assertTrue(retrievedBookOpt.isPresent());
        assertEquals(bookId, retrievedBookOpt.get().getBookId());
        verify(bookRepository, times(1)).findById(bookId);
    }

    @Test
    void shouldReturnEmptyOptionalWhenIdDoesNotExist() {
        UUID bookId = UUID.randomUUID();
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        Optional<Book> retrievedBookOpt = bookService.getBookById(bookId);

        assertFalse(retrievedBookOpt.isPresent());
        verify(bookRepository, times(1)).findById(bookId);
    }

    @Test
    void shouldReturnListOfBooksWhenBooksExist() {
        List<Book> expectedBooks = Arrays.asList(CatalogTestUtils.createValidBook(), CatalogTestUtils.createValidBook());
        when(bookRepository.findAll(null, null, null)).thenReturn(expectedBooks);

        List<Book> retrievedBooks = bookService.getAllBooks(null, null, null);

        assertNotNull(retrievedBooks);
        assertEquals(expectedBooks.size(), retrievedBooks.size());
        verify(bookRepository, times(1)).findAll(null, null, null);
    }

    @Test
    void shouldReturnEmptyListWhenNoBooksExist() {
        when(bookRepository.findAll(null, null, null)).thenReturn(new ArrayList<>());

        List<Book> retrievedBooks = bookService.getAllBooks(null, null, null);

        assertNotNull(retrievedBooks);
        assertTrue(retrievedBooks.isEmpty());
        verify(bookRepository, times(1)).findAll(null, null, null);
    }

    @Test
    void shouldUpdateBookSuccessfully() {
        Book bookToUpdate = CatalogTestUtils.createValidBook();
        when(bookRepository.save(any(Book.class))).thenReturn(bookToUpdate);

        Book updatedBook = bookService.updateBook(bookToUpdate);

        assertNotNull(updatedBook);
        assertEquals(bookToUpdate.getBookId(), updatedBook.getBookId());
        verify(bookRepository, times(1)).save(bookToUpdate);
    }

    @Test
    void shouldDeleteBookById() {
        UUID bookIdToDelete = UUID.randomUUID();
        doNothing().when(bookRepository).deleteById(bookIdToDelete);

        bookService.deleteBookById(bookIdToDelete);

        verify(bookRepository, times(1)).deleteById(bookIdToDelete);
    }

    @Test
    void shouldDelegateSearchToRepository() {
        String query = "test";
        DomainPage<Book> mockDomainPage = new DomainPage<>(Collections.emptyList(), 0, 0, 0, 10, true, true);
        when(bookRepository.searchBooks(anyString(), anyInt(), anyInt(), anyString(), anyString())).thenReturn(mockDomainPage);

        DomainPage<Book> resultPage = bookService.searchBooks(query, 0, 10, "title", "asc");

        verify(bookRepository, times(1)).searchBooks(query, 0, 10, "title", "asc");
        assertNotNull(resultPage);
    }
}