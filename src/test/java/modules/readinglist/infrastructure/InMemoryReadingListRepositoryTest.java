package modules.readinglist.infrastructure;

import modules.catalog.core.domain.Book;
import modules.catalog.utils.CatalogTestUtils;
import modules.readinglist.core.domain.ReadingList;
import modules.readinglist.core.domain.ReadingListImpl;
import modules.readinglist.infrastructure.persistence.in_memory.InMemoryReadingListRepository;
import modules.readinglist.utils.ReadingListTestUtils;
import modules.user.core.domain.User;
import modules.user.utils.UserTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import common.InMemoryRepositoryTestProfile;
import io.quarkus.test.junit.TestProfile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestProfile(InMemoryRepositoryTestProfile.class)
public class InMemoryReadingListRepositoryTest {
    private InMemoryReadingListRepository repository;

    private User testUser1;
    private User testUser2; 
    private Book testBook1;
    private Book testBook2;
    private Book testBook3; 
    private ReadingList testList1User1; 
    private ReadingList testList2User1; 
    private ReadingList testList1User2; 

    @BeforeEach
    void setUp() {
        repository = new InMemoryReadingListRepository();

        testUser1 = UserTestUtils.createValidUser();
        testUser2 = UserTestUtils.createValidUser();
        testBook1 = CatalogTestUtils.createValidBookWithId(UUID.randomUUID());
        testBook2 = CatalogTestUtils.createValidBookWithId(UUID.randomUUID());
        testBook3 = CatalogTestUtils.createValidBookWithId(UUID.randomUUID()); 

        testList1User1 = ReadingListTestUtils.createValidReadingListForUser(testUser1, "User1 List A");
        repository.create(testList1User1);
        
        testList2User1 = ReadingListTestUtils.createValidReadingListForUser(testUser1, "User1 List B");
        repository.create(testList2User1);

        testList1User2 = ReadingListTestUtils.createValidReadingListForUser(testUser2, "User2 List A");
        repository.create(testList1User2);
    }

    @Test
    void testCreateReadingList() {
        ReadingList newReadingList = ReadingListTestUtils.createValidReadingList();
        ReadingList savedReadingList = repository.create(newReadingList);
        assertNotNull(savedReadingList);
        assertEquals(newReadingList.getReadingListId(), savedReadingList.getReadingListId());
        assertTrue(repository.findById(newReadingList.getReadingListId()).isPresent());
    }

    @Test
    void testFindReadingListByIdSuccessful() {
        Optional<ReadingList> retrievedReadingListOpt =
                repository.findById(testList1User1.getReadingListId());
        assertTrue(retrievedReadingListOpt.isPresent());
        assertEquals(testList1User1.getReadingListId(),
                retrievedReadingListOpt.get().getReadingListId());
        assertEquals(testList1User1.getName(), retrievedReadingListOpt.get().getName());
    }

    @Test
    void testFindReadingListByIdFails() {
        UUID nonExistentId = UUID.randomUUID();
        Optional<ReadingList> retrievedReadingListOpt = repository.findById(nonExistentId);
        assertTrue(retrievedReadingListOpt.isEmpty());
    }

    @Test
    void testDeleteReadingListById() {
        repository.deleteById(testList1User1.getReadingListId());
        Optional<ReadingList> deletedReadingListOpt =
                repository.findById(testList1User1.getReadingListId());
        assertTrue(deletedReadingListOpt.isEmpty());
    }

    @Test
    void testUpdateReadingListMetadata() {
        ReadingList updatedMetadataList = ReadingListImpl.builder()
                .readingListId(testList1User1.getReadingListId())
                .user(testList1User1.getUser())
                .name("Updated Name")
                .description("Updated Description")
                .creationDate(testList1User1.getCreationDate())
                .books(testList1User1.getBooks()) 
                .build();
        
        ReadingList result = repository.update(updatedMetadataList);
        
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(testList1User1.getBooks().size(), result.getBooks().size());
    }

    @Test
    void testAddBookToReadingListSuccessful() {
        repository.addBookToReadingList(testList1User1.getReadingListId(), testBook1.getBookId());
        repository.addBookToReadingList(testList1User1.getReadingListId(), testBook2.getBookId());

        Optional<ReadingList> updatedListOpt = repository.findById(testList1User1.getReadingListId());
        assertTrue(updatedListOpt.isPresent());
        List<Book> booksInList = updatedListOpt.get().getBooks();
        assertEquals(2, booksInList.size());
        assertTrue(booksInList.contains(testBook1));
        assertTrue(booksInList.contains(testBook2));
    }

    @Test
    void testAddBookToReadingListAlreadyExistsReturnsQuietly() {
        repository.addBookToReadingList(testList1User1.getReadingListId(), testBook1.getBookId());
        
        
        repository.addBookToReadingList(testList1User1.getReadingListId(), testBook1.getBookId()); 

        Optional<ReadingList> updatedListOpt = repository.findById(testList1User1.getReadingListId());
        assertTrue(updatedListOpt.isPresent());
        assertEquals(1, updatedListOpt.get().getBooks().size()); 
        assertTrue(updatedListOpt.get().getBooks().contains(testBook1));
    }

    @Test
    void testAddBookToReadingListNotFound() {
        assertThrows(IllegalArgumentException.class, () -> {
            repository.addBookToReadingList(UUID.randomUUID(), testBook1.getBookId());
        }, "Should throw IllegalArgumentException when list not found");
    }

    @Test
    void testRemoveBookFromReadingListSuccessful() {
        repository.addBookToReadingList(testList1User1.getReadingListId(), testBook1.getBookId());
        repository.addBookToReadingList(testList1User1.getReadingListId(), testBook2.getBookId());

        repository.removeBookFromReadingList(testList1User1.getReadingListId(), testBook1.getBookId());

        Optional<ReadingList> updatedListOpt = repository.findById(testList1User1.getReadingListId());
        assertTrue(updatedListOpt.isPresent());
        List<Book> booksInList = updatedListOpt.get().getBooks();
        assertEquals(1, booksInList.size());
        assertFalse(booksInList.contains(testBook1));
        assertTrue(booksInList.contains(testBook2));
    }

    @Test
    void testRemoveBookFromReadingListBookNotFoundInList() {
        repository.addBookToReadingList(testList1User1.getReadingListId(), testBook1.getBookId());

        assertThrows(IllegalArgumentException.class, () -> {
            repository.removeBookFromReadingList(testList1User1.getReadingListId(), testBook2.getBookId());
        }, "Should throw IllegalArgumentException when book not in list");
    }

    @Test
    void testRemoveBookFromReadingListListNotFound() {
        assertThrows(IllegalArgumentException.class, () -> {
            repository.removeBookFromReadingList(UUID.randomUUID(), testBook1.getBookId());
        }, "Should throw IllegalArgumentException when list not found");
    }

    @Test
    void testGetBooksInReadingListSuccessful() {
        repository.addBookToReadingList(testList1User1.getReadingListId(), testBook1.getBookId());
        repository.addBookToReadingList(testList1User1.getReadingListId(), testBook2.getBookId());

        List<UUID> booksInList =
                repository.getBookIdsInReadingList(testList1User1.getReadingListId());

        assertFalse(booksInList.isEmpty());
        assertEquals(2, booksInList.size());
        assertTrue(booksInList.contains(testBook1.getBookId()));
        assertTrue(booksInList.contains(testBook2.getBookId()));
    }

    @Test
    void testGetBooksInReadingListEmpty() {
        
        List<UUID> booksInList = repository.getBookIdsInReadingList(testList1User1.getReadingListId());
        assertTrue(booksInList.isEmpty());
    }

    @Test
    void testGetBooksInReadingListNonExistentList() {
        List<UUID> booksInList = repository.getBookIdsInReadingList(UUID.randomUUID());
        assertTrue(booksInList.isEmpty());
    }

    @Test
    void testFindByUserIdSuccessful() {
        
        List<ReadingList> userReadingLists = repository.findByUserId(testUser1.getKeycloakUserId());

        assertEquals(2, userReadingLists.size());
        
        assertTrue(userReadingLists.stream().anyMatch(rl -> rl.getReadingListId().equals(testList1User1.getReadingListId())));
        assertTrue(userReadingLists.stream().anyMatch(rl -> rl.getReadingListId().equals(testList2User1.getReadingListId())));
    }

    @Test
    void testFindByUserIdEmpty() {
        User userWithoutLists = UserTestUtils.createValidUser();
        List<ReadingList> userReadingLists = repository.findByUserId(userWithoutLists.getKeycloakUserId());
        assertTrue(userReadingLists.isEmpty());
    }

    @Test
    void testFindByUserIdNotExists() {
        UUID nonExistentUserId = UUID.randomUUID();
        List<ReadingList> userReadingLists = repository.findByUserId(nonExistentUserId);
        assertTrue(userReadingLists.isEmpty());
    }

    @Test
    void testFindReadingListContainingBookForUserSuccessful() {
        repository.addBookToReadingList(testList1User1.getReadingListId(), testBook1.getBookId());
        
        Optional<ReadingList> foundListOpt = repository.findReadingListContainingBookForUser(testUser1.getKeycloakUserId(), testBook1.getBookId());

        assertTrue(foundListOpt.isPresent());
        assertEquals(testList1User1.getReadingListId(), foundListOpt.get().getReadingListId());
        assertTrue(foundListOpt.get().getBooks().contains(testBook1));
    }

    @Test
    void testFindReadingListContainingBookForUserBookNotFound() {
        
        Optional<ReadingList> foundListOpt = repository.findReadingListContainingBookForUser(testUser1.getKeycloakUserId(), testBook3.getBookId());

        assertFalse(foundListOpt.isPresent());
    }

    @Test
    void testFindReadingListContainingBookForUserUserNotFound() {
        repository.addBookToReadingList(testList1User1.getReadingListId(), testBook1.getBookId());
        
        Optional<ReadingList> foundListOpt = repository.findReadingListContainingBookForUser(UUID.randomUUID(), testBook1.getBookId());

        assertFalse(foundListOpt.isPresent());
    }

    @Test
    void testFindReadingListContainingBookForUserBookInAnotherUsersList() {
        
        repository.addBookToReadingList(testList1User1.getReadingListId(), testBook1.getBookId()); 
        
        
        Optional<ReadingList> foundListOpt = repository.findReadingListContainingBookForUser(testUser2.getKeycloakUserId(), testBook1.getBookId());

        assertFalse(foundListOpt.isPresent());
    }

}