package modules.catalog.infrastructure;

import modules.catalog.core.domain.Book;
import modules.catalog.core.domain.DomainPage;
import modules.catalog.core.usecases.repositories.BookRepository;
import modules.catalog.utils.CatalogTestUtils;
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
        Book savedBook = repository.save(bookToSave);
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
        repository.save(CatalogTestUtils.createValidBook());
        repository.save(CatalogTestUtils.createValidBook());
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
        Book savedBook = repository.save(bookToDelete);
        repository.deleteById(savedBook.getBookId());
        Optional<Book> deletedBook = repository.findById(savedBook.getBookId());
        assertFalse(deletedBook.isPresent());
    }

    @Test
    void shouldSearchBooksByTitleWithPagination() {
        repository.save(CatalogTestUtils.createTestBookWithDate("The Great Gatsby", "A novel by F. Scott Fitzgerald.", LocalDate.of(1925, 4, 10)));
        repository.save(CatalogTestUtils.createTestBookWithDate("Gatsby's Dream", "A sequel to the great Gatsby.", LocalDate.of(2000, 1, 1)));
        repository.save(CatalogTestUtils.createTestBookWithDate("Moby Dick", "A classic about a whale.", LocalDate.of(1851, 10, 18)));

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
        repository.save(CatalogTestUtils.createTestBook("Book A", "This is a test description about cats."));
        repository.save(CatalogTestUtils.createTestBook("Book B", "Another description about dogs."));
        repository.save(CatalogTestUtils.createTestBook("Book C", "A story featuring a cat."));

        DomainPage<Book> results = repository.searchBooks("cat", 0, 10, "title", "asc");

        assertEquals(2, results.content().size());
        assertEquals(2, results.totalElements());
        assertEquals("Book A", results.content().get(0).getTitle());
        assertEquals("Book C", results.content().get(1).getTitle());
    }

    @Test
    void shouldReturnEmptyPageIfNoResultsFound() {
        repository.save(CatalogTestUtils.createValidBook());
        DomainPage<Book> results = repository.searchBooks("xyz", 0, 10, "title", "asc");

        assertTrue(results.content().isEmpty());
        assertEquals(0, results.totalElements());
        assertEquals(0, results.totalPages());
    }
}