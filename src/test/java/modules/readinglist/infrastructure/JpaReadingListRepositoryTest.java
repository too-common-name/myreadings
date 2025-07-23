package modules.readinglist.infrastructure;

import common.JpaRepositoryTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.TestTransaction;
import jakarta.inject.Inject;
import modules.catalog.core.domain.Book;
import modules.catalog.core.usecases.repositories.BookRepository;
import modules.catalog.utils.CatalogTestUtils;
import modules.readinglist.core.domain.ReadingList;
import modules.readinglist.core.usecases.repositories.ReadingListRepository;
import modules.user.core.domain.User;
import modules.user.core.usecases.repositories.UserRepository;
import modules.readinglist.utils.ReadingListTestUtils;
import modules.user.utils.UserTestUtils;
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
    private Book testBook3;
    private ReadingList persistedList1User1;
    private ReadingList persistedList2User1;

    private void setupTestData() {
        testUser1 = userRepository.save(UserTestUtils.createValidUser());
        testUser2 = userRepository.save(UserTestUtils.createValidUser());
        testBook1 = bookRepository.save(CatalogTestUtils.createValidBook());
        testBook2 = bookRepository.save(CatalogTestUtils.createValidBook());
        testBook3 = bookRepository.save(CatalogTestUtils.createValidBook());
        persistedList1User1 = readingListRepository.create(ReadingListTestUtils.createValidReadingListForUser(testUser1, "User1 List A"));
        persistedList2User1 = readingListRepository.create(ReadingListTestUtils.createValidReadingListForUser(testUser1, "User1 List B"));
    }

    @Test
    @TestTransaction
    void testCreateAndFindById() {
        setupTestData();
        ReadingList newReadingList = ReadingListTestUtils.createValidReadingListForUser(testUser1, "New JPA List");
        ReadingList savedReadingList = readingListRepository.create(newReadingList);
        Optional<ReadingList> retrieved = readingListRepository.findById(savedReadingList.getReadingListId());
        assertTrue(retrieved.isPresent());
        assertEquals(savedReadingList.getReadingListId(), retrieved.get().getReadingListId());
        assertEquals(newReadingList.getName(), retrieved.get().getName());
    }

    @Test
    @TestTransaction
    void testDeleteReadingListById() {
        setupTestData();
        ReadingList listToDelete = readingListRepository.create(ReadingListTestUtils.createValidReadingListForUser(testUser1, "To Delete"));
        UUID listId = listToDelete.getReadingListId();
        readingListRepository.deleteById(listId);
        assertTrue(readingListRepository.findById(listId).isEmpty());
    }

    @Test
    @TestTransaction
    void testAddAndGetBooksInReadingList() {
        setupTestData();
        readingListRepository.addBookToReadingList(persistedList1User1.getReadingListId(), testBook1);
        readingListRepository.addBookToReadingList(persistedList1User1.getReadingListId(), testBook2);
        List<Book> booksInList = readingListRepository.getBooksInReadingList(persistedList1User1.getReadingListId());
        assertEquals(2, booksInList.size());
        assertTrue(booksInList.contains(testBook1));
        assertTrue(booksInList.contains(testBook2));
    }

    @Test
    @TestTransaction
    void testAddBookToReadingListAlreadyExistsReturnsQuietly() {
        setupTestData();
        readingListRepository.addBookToReadingList(persistedList1User1.getReadingListId(), testBook1);
        readingListRepository.addBookToReadingList(persistedList1User1.getReadingListId(), testBook1);
        List<Book> booksInList = readingListRepository.getBooksInReadingList(persistedList1User1.getReadingListId());
        assertEquals(1, booksInList.size());
        assertTrue(booksInList.contains(testBook1));
    }

    @Test
    @TestTransaction
    void testAddBookToReadingListNotFound() {
        setupTestData();
        UUID nonExistentListId = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () -> {
            readingListRepository.addBookToReadingList(nonExistentListId, testBook1);
        });
    }

    @Test
    @TestTransaction
    void testRemoveBookFromReadingList() {
        setupTestData();
        readingListRepository.addBookToReadingList(persistedList1User1.getReadingListId(), testBook1);
        readingListRepository.addBookToReadingList(persistedList1User1.getReadingListId(), testBook2);
        readingListRepository.removeBookFromReadingList(persistedList1User1.getReadingListId(), testBook2.getBookId());
        List<Book> booksInList = readingListRepository.getBooksInReadingList(persistedList1User1.getReadingListId());
        assertEquals(1, booksInList.size());
        assertEquals(testBook1.getBookId(), booksInList.get(0).getBookId());
    }

    @Test
    @TestTransaction
    void testRemoveBookFromReadingListBookNotFoundInList() {
        setupTestData();
        readingListRepository.addBookToReadingList(persistedList1User1.getReadingListId(), testBook1);
        assertThrows(IllegalArgumentException.class, () -> {
            readingListRepository.removeBookFromReadingList(persistedList1User1.getReadingListId(), testBook2.getBookId());
        });
    }

    @Test
    @TestTransaction
    void testRemoveBookFromReadingListListNotFound() {
        setupTestData();
        assertThrows(IllegalArgumentException.class, () -> {
            readingListRepository.removeBookFromReadingList(UUID.randomUUID(), testBook1.getBookId());
        });
    }

    @Test
    @TestTransaction
    void testGetBooksInReadingListSuccessful() {
        setupTestData();
        readingListRepository.addBookToReadingList(persistedList1User1.getReadingListId(), testBook1);
        readingListRepository.addBookToReadingList(persistedList1User1.getReadingListId(), testBook2);
        List<Book> booksInList = readingListRepository.getBooksInReadingList(persistedList1User1.getReadingListId());
        assertFalse(booksInList.isEmpty());
        assertEquals(2, booksInList.size());
        assertTrue(booksInList.contains(testBook1));
        assertTrue(booksInList.contains(testBook2));
    }

    @Test
    @TestTransaction
    void testGetBooksInReadingListEmpty() {
        setupTestData();
        ReadingList emptyList = readingListRepository.create(ReadingListTestUtils.createValidReadingListForUser(testUser1, "Empty JPA List"));
        List<Book> booksInList = readingListRepository.getBooksInReadingList(emptyList.getReadingListId());
        assertTrue(booksInList.isEmpty());
    }

    @Test
    @TestTransaction
    void testGetBooksInReadingListNonExistentList() {
        setupTestData();
        List<Book> booksInList = readingListRepository.getBooksInReadingList(UUID.randomUUID());
        assertTrue(booksInList.isEmpty());
    }

    @Test
    @TestTransaction
    void testFindByUserId() {
        setupTestData();
        List<ReadingList> userReadingLists = readingListRepository.findByUserId(testUser1.getKeycloakUserId());
        assertEquals(2, userReadingLists.size());
        assertTrue(userReadingLists.stream().anyMatch(rl -> rl.getReadingListId().equals(persistedList1User1.getReadingListId())));
        assertTrue(userReadingLists.stream().anyMatch(rl -> rl.getReadingListId().equals(persistedList2User1.getReadingListId())));
    }

    @Test
    @TestTransaction
    void testFindByUserIdEmpty() {
        setupTestData();
        User userWithoutLists = userRepository.save(UserTestUtils.createValidUser());
        List<ReadingList> userReadingLists = readingListRepository.findByUserId(userWithoutLists.getKeycloakUserId());
        assertTrue(userReadingLists.isEmpty());
    }

    @Test
    @TestTransaction
    void testFindByUserIdNotExists() {
        setupTestData();
        UUID nonExistentUserId = UUID.randomUUID();
        List<ReadingList> userReadingLists = readingListRepository.findByUserId(nonExistentUserId);
        assertTrue(userReadingLists.isEmpty());
    }

    @Test
    @TestTransaction
    void testFindReadingListContainingBookForUserSuccessful() {
        setupTestData();
        readingListRepository.addBookToReadingList(persistedList1User1.getReadingListId(), testBook1);
        Optional<ReadingList> foundListOpt = readingListRepository.findReadingListContainingBookForUser(testUser1.getKeycloakUserId(), testBook1.getBookId());
        assertTrue(foundListOpt.isPresent());
        assertEquals(persistedList1User1.getReadingListId(), foundListOpt.get().getReadingListId());
        assertTrue(foundListOpt.get().getBooks().stream().anyMatch(b -> b.getBookId().equals(testBook1.getBookId())));
    }

    @Test
    @TestTransaction
    void testFindReadingListContainingBookForUserBookNotFound() {
        setupTestData();
        Optional<ReadingList> foundListOpt = readingListRepository.findReadingListContainingBookForUser(testUser1.getKeycloakUserId(), testBook3.getBookId());
        assertFalse(foundListOpt.isPresent());
    }

    @Test
    @TestTransaction
    void testFindReadingListContainingBookForUserUserNotFound() {
        setupTestData();
        readingListRepository.addBookToReadingList(persistedList1User1.getReadingListId(), testBook1);
        Optional<ReadingList> foundListOpt = readingListRepository.findReadingListContainingBookForUser(UUID.randomUUID(), testBook1.getBookId());
        assertFalse(foundListOpt.isPresent());
    }

    @Test
    @TestTransaction
    void testFindReadingListContainingBookForUserBookInAnotherUsersList() {
        setupTestData();
        readingListRepository.addBookToReadingList(persistedList1User1.getReadingListId(), testBook1);
        Optional<ReadingList> foundListOpt = readingListRepository.findReadingListContainingBookForUser(testUser2.getKeycloakUserId(), testBook1.getBookId());
        assertFalse(foundListOpt.isPresent());
    }
}
