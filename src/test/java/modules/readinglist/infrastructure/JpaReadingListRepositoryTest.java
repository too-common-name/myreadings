package modules.readinglist.infrastructure;

import common.JpaRepositoryTestProfile;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import modules.catalog.core.domain.Book;
import modules.catalog.core.usecases.repositories.BookRepository;
import modules.catalog.utils.CatalogTestUtils;
import modules.readinglist.core.domain.ReadingList;
import modules.readinglist.core.usecases.repositories.ReadingListRepository;
import modules.readinglist.utils.ReadingListTestUtils;
import modules.user.core.domain.User;
import modules.user.core.usecases.repositories.UserRepository;
import modules.user.utils.UserTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestProfile(JpaRepositoryTestProfile.class)
@TestTransaction
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

    @BeforeEach
    void setUp() {
        testUser1 = userRepository.save(UserTestUtils.createValidUser());
        testUser2 = userRepository.save(UserTestUtils.createValidUser());
        testBook1 = bookRepository.save(CatalogTestUtils.createValidBook());
        testBook2 = bookRepository.save(CatalogTestUtils.createValidBook());
    }

    @Test
    void testSaveAndFindById() {
        ReadingList readingList = ReadingListTestUtils.createValidReadingListForUser(testUser1, "My List");
        
        ReadingList savedReadingList = readingListRepository.save(readingList);
        
        Optional<ReadingList> retrieved = readingListRepository.findById(savedReadingList.getReadingListId());
        
        assertTrue(retrieved.isPresent());
        assertEquals(savedReadingList.getReadingListId(), retrieved.get().getReadingListId());
    }

    @Test
    void testDeleteReadingListById() {
        ReadingList readingList = readingListRepository.save(
            ReadingListTestUtils.createValidReadingListForUser(testUser1, "To Delete")
        );
        UUID listId = readingList.getReadingListId();
        
        readingListRepository.deleteById(listId);
        
        assertTrue(readingListRepository.findById(listId).isEmpty());
    }

    @Test
    void testAddAndGetBooksInReadingList() {
        ReadingList savedReadingList = readingListRepository.save(
            ReadingListTestUtils.createValidReadingListForUser(testUser1, "Sci-Fi")
        );
        
        readingListRepository.addBookToReadingList(savedReadingList.getReadingListId(), testBook1);
        readingListRepository.addBookToReadingList(savedReadingList.getReadingListId(), testBook2);
        
        List<Book> booksInList = readingListRepository.getBooksInReadingList(savedReadingList.getReadingListId());
        
        assertEquals(2, booksInList.size());
        List<UUID> bookIds = booksInList.stream().map(Book::getBookId).collect(Collectors.toList());
        assertTrue(bookIds.contains(testBook1.getBookId()));
        assertTrue(bookIds.contains(testBook2.getBookId()));
    }

    @Test
    void testRemoveBookFromReadingList() {
        ReadingList savedReadingList = readingListRepository.save(
            ReadingListTestUtils.createValidReadingListForUser(testUser1, "My Books")
        );
        readingListRepository.addBookToReadingList(savedReadingList.getReadingListId(), testBook1);
        readingListRepository.addBookToReadingList(savedReadingList.getReadingListId(), testBook2);

        readingListRepository.removeBookFromReadingList(savedReadingList.getReadingListId(), testBook2.getBookId());

        List<Book> booksInList = readingListRepository.getBooksInReadingList(savedReadingList.getReadingListId());
        assertEquals(1, booksInList.size());
        assertEquals(testBook1.getBookId(), booksInList.get(0).getBookId());
    }

    @Test
    void testFindByUserId() {
        readingListRepository.save(ReadingListTestUtils.createValidReadingListForUser(testUser1, "List 1"));
        readingListRepository.save(ReadingListTestUtils.createValidReadingListForUser(testUser1, "List 2"));
        readingListRepository.save(ReadingListTestUtils.createValidReadingListForUser(testUser2, "Other List"));

        List<ReadingList> userReadingLists = readingListRepository.findByUserId(testUser1.getKeycloakUserId());

        assertEquals(2, userReadingLists.size());
    }
}