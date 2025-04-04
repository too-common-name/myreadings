package modules.catalog.infrastructure;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import modules.catalog.core.usecases.repositories.BookRepository;
import modules.catalog.core.domain.Book;
import modules.catalog.utils.CatalogTestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class JpaBookRepositoryTest {

    @Inject
    BookRepository repository;

    @PersistenceContext
    EntityManager entityManager;
    
    @BeforeEach
    @Transactional
    void setUp() {
        entityManager.createQuery("DELETE FROM BookEntity").executeUpdate();
    }

    @Test
    @Transactional
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
    @Transactional
    void findByIdNotFound() {
        Optional<Book> retrievedBook = repository.findById(UUID.randomUUID());
        assertFalse(retrievedBook.isPresent());
    }

    @Test
    @Transactional
    void findAllSuccessful() {
        repository.save(CatalogTestUtils.createValidBook());
        repository.save(CatalogTestUtils.createValidBook());
        List<Book> allBooks = repository.findAll();
        assertEquals(2, allBooks.size());
    }

    @Test
    @Transactional
    void findAllSuccessfulNoBooks() {
        List<Book> allBooks = repository.findAll();
        assertTrue(allBooks.isEmpty());
    }

    @Test
    @Transactional
    void deleteByIdSuccessful() {
        Book bookToDelete = CatalogTestUtils.createValidBook();
        Book savedBook = repository.save(bookToDelete);
        repository.deleteById(savedBook.getBookId());
        Optional<Book> deletedBook = repository.findById(savedBook.getBookId());
        assertFalse(deletedBook.isPresent());
    }
}