package modules.catalog.infrastructure;

import modules.catalog.core.domain.Book;
import modules.catalog.infrastructure.persistence.in_memory.InMemoryBookRepository;
import modules.catalog.utils.CatalogTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import common.InMemoryRepositoryTestProfile;
import io.quarkus.test.junit.TestProfile;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestProfile(InMemoryRepositoryTestProfile.class)
public class InMemoryBookRepositoryTest {

    private InMemoryBookRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryBookRepository();
    }

    @Test
    void saveAndFindByIdSuccessful() {
        Book bookToSave = CatalogTestUtils.createValidBook();
        Book savedBook = repository.save(bookToSave);
        Optional<Book> retrievedBook = repository.findById(savedBook.getBookId());
        assertTrue(retrievedBook.isPresent());
        assertEquals(savedBook.getBookId(), retrievedBook.get().getBookId());
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
        List<Book> allBooks = repository.findAll();
        assertEquals(2, allBooks.size());
    }

    @Test
    void findAllSuccessfulNoBooks() {
        List<Book> allBooks = repository.findAll();
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
}