package modules.catalog.infrastructure;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import modules.catalog.core.usecases.repositories.BookRepository;
import modules.catalog.core.domain.Book;
import modules.catalog.utils.CatalogTestUtils;

import org.junit.jupiter.api.Test;

import common.JpaRepositoryTestProfile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestProfile(JpaRepositoryTestProfile.class)
@TestTransaction
public class JpaBookRepositoryTest {

    @Inject
    BookRepository repository;

    @Test
    void saveAndFindByIdSuccessful() {
        Book bookToSave = CatalogTestUtils.createValidBook();
        Book savedBook = repository.save(bookToSave);
        Optional<Book> retrievedBook = repository.findById(savedBook.getBookId());
        assertTrue(retrievedBook.isPresent());
        assertEquals(savedBook.getBookId(), retrievedBook.get().getBookId());
        assertEquals(savedBook.getIsbn(), retrievedBook.get().getIsbn());
        assertEquals(savedBook.getTitle(), retrievedBook.get().getTitle());
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
}