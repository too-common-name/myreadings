package modules.readinglist.infrastructure;

import common.JpaRepositoryTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.TestTransaction;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import modules.catalog.core.domain.Book;
import modules.catalog.core.usecases.repositories.BookRepository;
import modules.catalog.utils.CatalogTestUtils;
import modules.readinglist.core.domain.ReadingList;
import modules.readinglist.core.usecases.repositories.ReadingListRepository;
import modules.user.core.domain.User;
import modules.user.core.usecases.repositories.UserRepository;
import modules.readinglist.utils.ReadingListTestUtils;
import modules.user.utils.UserTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestProfile(JpaRepositoryTestProfile.class)
public class JpaReadingListRepositoryTest {

    @Inject
    ReadingListRepository readingListRepository;
    @Inject
    UserRepository userRepository;
    @Inject
    BookRepository bookRepository;

    private User testUser1;
    private User testUser2;
    private Book testBook1;
    private Book testBook2;
    private ReadingList persistedList1User1;

    @BeforeEach
    void setUp() {
        setupUsers();
        setupBooks();
        setupReadingLists();
    }
    
    @Transactional
    void setupUsers() {
        testUser1 = userRepository.save(UserTestUtils.createValidUser());
        testUser2 = userRepository.save(UserTestUtils.createValidUser());
    }

    @Transactional
    void setupBooks() {
        testBook1 = bookRepository.save(CatalogTestUtils.createValidBook());
        testBook2 = bookRepository.save(CatalogTestUtils.createValidBook());
    }
    
    @Transactional
    void setupReadingLists() {
        persistedList1User1 = readingListRepository.create(ReadingListTestUtils.createValidReadingListForUser(testUser1, "User1 List A"));
    }

    @Test
    @TestTransaction
    void testCreateAndFindById() {
        ReadingList newReadingList = ReadingListTestUtils.createValidReadingListForUser(testUser2, "New JPA List");
        ReadingList savedReadingList = readingListRepository.create(newReadingList);
        Optional<ReadingList> retrieved = readingListRepository.findById(savedReadingList.getReadingListId());
        assertTrue(retrieved.isPresent());
        assertEquals(savedReadingList.getReadingListId(), retrieved.get().getReadingListId());
    }

    @Test
    @TestTransaction
    void testDeleteReadingListById() {
        UUID listId = persistedList1User1.getReadingListId();
        readingListRepository.deleteById(listId);
        assertTrue(readingListRepository.findById(listId).isEmpty());
    }

    @Test
    @TestTransaction
    void testAddAndGetBooksInReadingList() {
        readingListRepository.addBookToReadingList(persistedList1User1.getReadingListId(), testBook1.getBookId());
        readingListRepository.addBookToReadingList(persistedList1User1.getReadingListId(), testBook2.getBookId());
        List<UUID> booksInList = readingListRepository.getBookIdsInReadingList(persistedList1User1.getReadingListId());
        assertEquals(2, booksInList.size());
    }

    @Test
    @TestTransaction
    void testRemoveBookFromReadingList() {
        readingListRepository.addBookToReadingList(persistedList1User1.getReadingListId(), testBook1.getBookId());
        readingListRepository.addBookToReadingList(persistedList1User1.getReadingListId(), testBook2.getBookId());
        readingListRepository.removeBookFromReadingList(persistedList1User1.getReadingListId(), testBook2.getBookId());
        List<UUID> booksInList = readingListRepository.getBookIdsInReadingList(persistedList1User1.getReadingListId());
        assertEquals(1, booksInList.size());
        assertEquals(testBook1.getBookId(), booksInList.get(0));
    }

    @Test
    @TestTransaction
    void testFindByUserId() {
        readingListRepository.create(ReadingListTestUtils.createValidReadingListForUser(testUser1, "User1 List B"));
        List<ReadingList> userReadingLists = readingListRepository.findByUserId(testUser1.getKeycloakUserId());
        assertEquals(2, userReadingLists.size());
    }

    @Test
    @TestTransaction
    void testFindReadingListContainingBookForUserSuccessful() {
        readingListRepository.addBookToReadingList(persistedList1User1.getReadingListId(), testBook1.getBookId());
        Optional<ReadingList> foundListOpt = readingListRepository.findReadingListContainingBookForUser(testUser1.getKeycloakUserId(), testBook1.getBookId());
        assertTrue(foundListOpt.isPresent());
        assertEquals(persistedList1User1.getReadingListId(), foundListOpt.get().getReadingListId());
    }
}