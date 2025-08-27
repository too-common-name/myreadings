package org.modular.playground.catalog.infrastructure;

import org.modular.playground.catalog.core.domain.Book;
import org.modular.playground.catalog.core.domain.DomainPage;
import org.modular.playground.catalog.core.usecases.repositories.BookRepository;
import org.modular.playground.catalog.utils.CatalogTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractBookRepositoryTest {

    private BookRepository repository;

    protected abstract BookRepository getRepository();

    @BeforeEach
    void setUp() {
        this.repository = getRepository();
    }

    @Test
    void saveAndFindByIdSuccessful() {
        Book bookToSave = CatalogTestUtils.createValidBook();
        Book savedBook = repository.create(bookToSave);
        Optional<Book> retrievedBook = repository.findById(savedBook.getBookId());

        assertTrue(retrievedBook.isPresent());
        assertEquals(savedBook.getBookId(), retrievedBook.get().getBookId());
        assertEquals(bookToSave.getTitle(), retrievedBook.get().getTitle());
    }

    @Test
    void findByIdNotFound() {
        Optional<Book> retrievedBook = repository.findById(UUID.randomUUID());
        assertFalse(retrievedBook.isPresent());
    }

    @Test
    void findAllSuccessful() {
        repository.create(CatalogTestUtils.createValidBook());
        repository.create(CatalogTestUtils.createValidBook());
        List<Book> allBooks = repository.findAll(null, null, null);
        assertEquals(2, allBooks.size());
    }

    @Test
    void findAllSuccessfulNoBooks() {
        List<Book> allBooks = repository.findAll(null, null, null);
        assertTrue(allBooks.isEmpty());
    }

    @Test
    void deleteByIdSuccessful() {
        Book bookToDelete = CatalogTestUtils.createValidBook();
        Book savedBook = repository.create(bookToDelete);
        repository.deleteById(savedBook.getBookId());
        Optional<Book> deletedBook = repository.findById(savedBook.getBookId());
        assertFalse(deletedBook.isPresent());
    }

    @Test
    void shouldSearchBooksByTitleWithPagination() {
        repository.create(CatalogTestUtils.createTestBookWithDate("The Great Gatsby", "A novel by F. Scott Fitzgerald.", LocalDate.of(1925, 4, 10)));
        repository.create(CatalogTestUtils.createTestBookWithDate("Gatsby's Dream", "A sequel to the great Gatsby.", LocalDate.of(2000, 1, 1)));
        repository.create(CatalogTestUtils.createTestBookWithDate("Moby Dick", "A classic about a whale.", LocalDate.of(1851, 10, 18)));

        DomainPage<Book> page1 = repository.searchBooks("gatsby", 0, 2, "title", "asc");

        assertEquals(2, page1.content().size());
        assertEquals(2, page1.totalElements());
        assertEquals(1, page1.totalPages());
        assertEquals("Gatsby's Dream", page1.content().get(0).getTitle());
        assertEquals("The Great Gatsby", page1.content().get(1).getTitle());
        assertTrue(page1.isLast());
        assertTrue(page1.isFirst());
    }

    @Test
    void shouldSearchBooksByDescriptionWithPagination() {
        repository.create(CatalogTestUtils.createTestBook("Book A", "This is a test description about cats."));
        repository.create(CatalogTestUtils.createTestBook("Book B", "Another description about dogs."));
        repository.create(CatalogTestUtils.createTestBook("Book C", "A story featuring a cat."));

        DomainPage<Book> results = repository.searchBooks("cat", 0, 10, "title", "asc");

        assertEquals(2, results.content().size());
        assertEquals(2, results.totalElements());
        assertEquals("Book A", results.content().get(0).getTitle());
        assertEquals("Book C", results.content().get(1).getTitle());
    }

    @Test
    void shouldReturnEmptyPageIfNoResultsFound() {
        repository.create(CatalogTestUtils.createValidBook());
        DomainPage<Book> results = repository.searchBooks("xyz", 0, 10, "title", "asc");

        assertTrue(results.content().isEmpty());
        assertEquals(0, results.totalElements());
        assertEquals(0, results.totalPages());
    }

    @Test
    void updateShouldThrowExceptionWhenBookIdIsNull() {
        Book bookWithNullId = CatalogTestUtils.createValidBookWithId(null);
        
        assertThrows(IllegalArgumentException.class, () -> {
            repository.update(bookWithNullId);
        });
    }

    @Test
    void updateShouldThrowExceptionWhenBookDoesNotExist() {
        Book nonExistentBook = CatalogTestUtils.createValidBook();
        
        assertThrows(RuntimeException.class, () -> {
            repository.update(nonExistentBook);
        });
    }

    @Test
    void searchBooksWithNullQueryShouldReturnEmptyPage() {
        repository.create(CatalogTestUtils.createValidBook());
        DomainPage<Book> results = repository.searchBooks(null, 0, 10, "title", "asc");

        assertTrue(results.content().isEmpty());
        assertEquals(0, results.totalElements());
    }

    @Test
    void searchBooksWithBlankQueryShouldReturnEmptyPage() {
        repository.create(CatalogTestUtils.createValidBook());
        DomainPage<Book> results = repository.searchBooks("   ", 0, 10, "title", "asc");
        
        assertTrue(results.content().isEmpty());
        assertEquals(0, results.totalElements());
    }

    @Test
    void shouldFindBooksByIds() {
        Book book1 = repository.create(CatalogTestUtils.createTestBook("Book One", "Desc 1"));
        Book book2 = repository.create(CatalogTestUtils.createTestBook("Book Two", "Desc 2"));
        repository.create(CatalogTestUtils.createTestBook("Book Three", "Desc 3")); // Questo non verrà cercato

        List<UUID> idsToFind = List.of(book1.getBookId(), book2.getBookId());
        List<Book> foundBooks = repository.findByIds(idsToFind);

        assertEquals(2, foundBooks.size());
        assertTrue(foundBooks.stream().anyMatch(b -> b.getBookId().equals(book1.getBookId())));
        assertTrue(foundBooks.stream().anyMatch(b -> b.getBookId().equals(book2.getBookId())));
    }

    @Test
    void shouldReturnEmptyListWhenFindingByIdsWithEmptyList() {
        List<Book> foundBooks = repository.findByIds(List.of());
        assertTrue(foundBooks.isEmpty());
    }

    @Test
    void shouldFindAllAndSortByTitleAsc() {
        repository.create(CatalogTestUtils.createTestBook("Zebra", ""));
        repository.create(CatalogTestUtils.createTestBook("Apple", ""));
        
        List<Book> allBooks = repository.findAll("title", "asc", null);

        assertEquals(2, allBooks.size());
        assertEquals("Apple", allBooks.get(0).getTitle());
        assertEquals("Zebra", allBooks.get(1).getTitle());
    }

    @Test
    void shouldFindAllAndSortByTitleDesc() {
        repository.create(CatalogTestUtils.createTestBook("Zebra", ""));
        repository.create(CatalogTestUtils.createTestBook("Apple", ""));

        List<Book> allBooks = repository.findAll("title", "desc", null);

        assertEquals(2, allBooks.size());
        assertEquals("Zebra", allBooks.get(0).getTitle());
        assertEquals("Apple", allBooks.get(1).getTitle());
    }

    @Test
    void shouldFindAllAndApplyLimit() {
        repository.create(CatalogTestUtils.createTestBook("Book 1", ""));
        repository.create(CatalogTestUtils.createTestBook("Book 2", ""));
        repository.create(CatalogTestUtils.createTestBook("Book 3", ""));
        
        List<Book> allBooks = repository.findAll(null, null, 2);

        assertEquals(2, allBooks.size());
    }

    @Test
    void shouldIgnoreInvalidSortField() {
        repository.create(CatalogTestUtils.createTestBook("Book A", ""));
        repository.create(CatalogTestUtils.createTestBook("Book B", ""));

        List<Book> allBooks = repository.findAll("invalidField", "asc", null);
        
        assertEquals(2, allBooks.size());
    }
}