package org.modular.playground.catalog.usecases;

import org.modular.playground.catalog.core.domain.Book;
import org.modular.playground.catalog.core.domain.BookImpl;
import org.modular.playground.catalog.core.domain.DomainPage;
import org.modular.playground.catalog.core.usecases.BookServiceImpl;
import org.modular.playground.catalog.core.usecases.repositories.BookRepository;
import org.modular.playground.catalog.infrastructure.persistence.postgres.mapper.BookMapper;
import org.modular.playground.catalog.infrastructure.persistence.postgres.mapper.BookMapperImpl;
import org.modular.playground.catalog.utils.CatalogTestUtils;
import org.modular.playground.catalog.web.dto.BookRequestDTO;
import org.modular.playground.catalog.web.dto.BookUpdateDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

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

    @Spy
    private BookMapper bookMapper = new BookMapperImpl();

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

        when(bookRepository.create(bookCaptor.capture())).thenAnswer(invocation -> {
            Book bookToSave = invocation.getArgument(0);
            ((BookImpl) bookToSave).setBookId(UUID.randomUUID());
            return bookToSave;
        });

        Book createdBook = bookService.createBook(requestDTO);

        assertNotNull(createdBook);
        assertNotNull(createdBook.getBookId());
        assertEquals(requestDTO.getTitle(), createdBook.getTitle());
        verify(bookRepository, times(1)).create(any(Book.class));
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
        List<Book> expectedBooks = Arrays.asList(CatalogTestUtils.createValidBook(),
                CatalogTestUtils.createValidBook());
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
    void shouldUpdateBookSuccessfullyWhenBookExists() {
        UUID bookId = UUID.randomUUID();
        BookUpdateDTO updateDTO = BookUpdateDTO.builder().title("New Title").build();
        Book originalBook = CatalogTestUtils.createValidBookWithId(bookId);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(originalBook));
        when(bookRepository.update(any(Book.class))).thenAnswer(i -> i.getArgument(0));

        Optional<Book> updatedBookOpt = bookService.updateBook(bookId, updateDTO);

        assertTrue(updatedBookOpt.isPresent());
        assertEquals("New Title", updatedBookOpt.get().getTitle());

        ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository, times(1)).update(bookCaptor.capture());
        assertEquals("New Title", bookCaptor.getValue().getTitle());
    }

    @Test
    void shouldDeleteBookById() {
        UUID bookIdToDelete = UUID.randomUUID();
        when(bookRepository.deleteById(bookIdToDelete)).thenReturn(true);

        bookService.deleteBookById(bookIdToDelete);

        verify(bookRepository, times(1)).deleteById(bookIdToDelete);
    }

    @Test
    void shouldDelegateSearchToRepository() {
        String query = "test";
        DomainPage<Book> mockDomainPage = new DomainPage<>(Collections.emptyList(), 0, 0, 0, 10, true, true);
        when(bookRepository.searchBooks(anyString(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(mockDomainPage);

        DomainPage<Book> resultPage = bookService.searchBooks(query, 0, 10, "title", "asc");

        verify(bookRepository, times(1)).searchBooks(query, 0, 10, "title", "asc");
        assertNotNull(resultPage);
    }

    @Test
    void shouldGetBooksByIdsWhenIdsAreProvided() {
        List<UUID> bookIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        List<Book> expectedBooks = List.of(
            CatalogTestUtils.createValidBookWithId(bookIds.get(0)),
            CatalogTestUtils.createValidBookWithId(bookIds.get(1))
        );

        when(bookRepository.findByIds(bookIds)).thenReturn(expectedBooks);

        List<Book> resultBooks = bookService.getBooksByIds(bookIds);

        assertNotNull(resultBooks);
        assertEquals(2, resultBooks.size());
        verify(bookRepository, times(1)).findByIds(bookIds);
    }
}