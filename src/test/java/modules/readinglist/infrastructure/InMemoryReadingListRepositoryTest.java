package modules.readinglist.infrastructure;

import modules.catalog.domain.Book;
import modules.readinglist.domain.ReadingList;
import modules.catalog.utils.CatalogTestUtils;
import modules.readinglist.utils.ReadingListTestUtils;
import modules.user.domain.User;
import modules.user.utils.UserTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InMemoryReadingListRepositoryTest {
    private InMemoryReadingListRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryReadingListRepository();
    }

    @Test
    void testSaveReadingList() {
        ReadingList readingList = ReadingListTestUtils.createValidReadingList();
        ReadingList savedReadingList = repository.save(readingList);
        assertNotNull(savedReadingList);
        assertEquals(readingList.getReadingListId(), savedReadingList.getReadingListId());
    }

    @Test
    void testFindReadingListByIdSuccessful() {
        ReadingList readingList = ReadingListTestUtils.createValidReadingList();
        ReadingList savedReadingList = repository.save(readingList);
        Optional<ReadingList> retrievedReadingListOpt =
                repository.findById(savedReadingList.getReadingListId());
        assertTrue(retrievedReadingListOpt.isPresent());
        assertEquals(savedReadingList.getReadingListId(),
                retrievedReadingListOpt.get().getReadingListId());
    }

    @Test
    void testFindReadingListByIdFails() {
        UUID nonExistentId = UUID.randomUUID();
        Optional<ReadingList> retrievedReadingListOpt = repository.findById(nonExistentId);
        assertTrue(retrievedReadingListOpt.isEmpty());
    }

    @Test
    void testDeleteReadingListById() {
        ReadingList readingList = ReadingListTestUtils.createValidReadingList();
        ReadingList savedReadingList = repository.save(readingList);
        repository.deleteById(savedReadingList.getReadingListId());
        Optional<ReadingList> deletedReadingListOpt =
                repository.findById(savedReadingList.getReadingListId());
        assertTrue(deletedReadingListOpt.isEmpty());
    }

    @Test
    void testAddBookToReadingList() {
        ReadingList readingList = ReadingListTestUtils.createValidReadingList();
        ReadingList savedReadingList = repository.save(readingList);
        Book book1 = CatalogTestUtils.createValidBookWithId(UUID.randomUUID());
        Book book2 = CatalogTestUtils.createValidBookWithId(UUID.randomUUID());

        repository.addBookToReadingList(savedReadingList.getReadingListId(), book1);
        repository.addBookToReadingList(savedReadingList.getReadingListId(), book2);

        List<Book> booksInList =
                repository.getBooksInReadingList(savedReadingList.getReadingListId());
        assertIterableEquals(List.of(book1, book2), booksInList);
    }

    @Test
    void testRemoveBookFromReadingList() {
        Book bookToRemove = CatalogTestUtils.createValidBookWithId(UUID.randomUUID());
        ReadingList readingList = ReadingListTestUtils.createValidReadingList();
        readingList.getBooks().add(bookToRemove);
        readingList.getBooks().add(CatalogTestUtils.createValidBookWithId(UUID.randomUUID()));
        ReadingList savedReadingList = repository.save(readingList);

        repository.removeBookFromReadingList(savedReadingList.getReadingListId(),
                bookToRemove.getBookId());

        List<Book> booksInList =
                repository.getBooksInReadingList(savedReadingList.getReadingListId());
        assertFalse(booksInList.contains(bookToRemove));
        assertEquals(1, booksInList.size());
    }

    @Test
    void testGetBooksInReadingListSuccessful() {
        ReadingList readingList = ReadingListTestUtils.createValidReadingList();
        Book book1 = CatalogTestUtils.createValidBookWithId(UUID.randomUUID());
        Book book2 = CatalogTestUtils.createValidBookWithId(UUID.randomUUID());
        readingList.getBooks().add(book1);
        readingList.getBooks().add(book2);
        ReadingList savedReadingList = repository.save(readingList);

        List<Book> booksInList =
                repository.getBooksInReadingList(savedReadingList.getReadingListId());

        assertFalse(booksInList.isEmpty());
        assertIterableEquals(List.of(book1, book2), booksInList);
    }

    @Test
    void testGetBooksInReadingListFails() {
        ReadingList readingList = ReadingListTestUtils.createValidReadingList();
        ReadingList savedReadingList = repository.save(readingList);
        List<Book> booksInList =
                repository.getBooksInReadingList(savedReadingList.getReadingListId());
        assertTrue(booksInList.isEmpty());
    }

    @Test
    void testFindByUserIdSuccessful() {
        User user = UserTestUtils.createValidUser();
        ReadingList readingList1 =
                ReadingListTestUtils.createValidReadingListForUser(user, "List 1");
        ReadingList readingList2 =
                ReadingListTestUtils.createValidReadingListForUser(user, "List 2");
        repository.save(readingList1);
        repository.save(readingList2);

        List<ReadingList> userReadingLists = repository.findByUserId(user.getUserId());

        assertEquals(2, userReadingLists.size());
        assertIterableEquals(new HashSet<>(List.of(readingList1, readingList2)),
                new HashSet<>(userReadingLists));
    }

    @Test
    void testFindByUserIdEmpty() {
        User user = UserTestUtils.createValidUser();
        List<ReadingList> userReadingLists = repository.findByUserId(user.getUserId());
        assertTrue(userReadingLists.isEmpty());
    }

    @Test
    void testFindByUserIdNotExists() {
        UUID nonExistentUserId = UUID.randomUUID();
        List<ReadingList> userReadingLists = repository.findByUserId(nonExistentUserId);
        assertTrue(userReadingLists.isEmpty());
    }
}
