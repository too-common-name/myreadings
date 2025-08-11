package modules.readinglist.infrastructure;

import modules.catalog.core.domain.Book;
import modules.readinglist.core.domain.ReadingList;
import modules.readinglist.core.usecases.repositories.ReadingListRepository;
import modules.readinglist.utils.ReadingListTestUtils;
import modules.user.core.domain.User;
import modules.user.utils.UserTestUtils;
import org.junit.jupiter.api.Test;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractReadingListRepositoryTest {

    protected ReadingListRepository repository;
    protected User testUser1;
    protected Book testBook1;
    protected ReadingList testList1;

    @Test
    @Transactional
    void shouldCreateAndFindById() {
        ReadingList newList = ReadingListTestUtils.createValidReadingListForUser(this.testUser1, "New List");
        ReadingList saved = repository.create(newList);
        Optional<ReadingList> found = repository.findById(saved.getReadingListId());
        
        assertTrue(found.isPresent());
        assertEquals(saved.getReadingListId(), found.get().getReadingListId());
    }

    @Test
    @Transactional
    void shouldDeleteById() {
        repository.deleteById(testList1.getReadingListId());
        Optional<ReadingList> found = repository.findById(testList1.getReadingListId());
        assertFalse(found.isPresent());
    }
    
    @Test
    @Transactional
    void shouldUpdateReadingList() {
        ReadingList toUpdate = ReadingListTestUtils.from(testList1).name("Updated Name").build();
        ReadingList updated = repository.update(toUpdate);
        assertEquals("Updated Name", updated.getName());
    }

    @Test
    @Transactional
    void shouldAddAndRemoveBook() {
        repository.addBookToReadingList(testList1.getReadingListId(), testBook1.getBookId());
        List<UUID> bookIds = repository.getBookIdsInReadingList(testList1.getReadingListId());
        assertEquals(1, bookIds.size());
        assertTrue(bookIds.contains(testBook1.getBookId()));
        repository.removeBookFromReadingList(testList1.getReadingListId(), testBook1.getBookId());
        bookIds = repository.getBookIdsInReadingList(testList1.getReadingListId());
        assertTrue(bookIds.isEmpty());
    }

    @Test
    @Transactional
    void shouldFindByUserId() {
        ReadingList testList2 = ReadingListTestUtils.createValidReadingListForUser(testUser1, "My Other List");
        repository.create(testList2);
        List<ReadingList> results = repository.findByUserId(testUser1.getKeycloakUserId());
        assertEquals(2, results.size());
    }
    
    @Test
    @Transactional
    void shouldFindReadingListContainingBookForUser() {
        repository.addBookToReadingList(testList1.getReadingListId(), testBook1.getBookId());
        Optional<ReadingList> result = repository.findReadingListContainingBookForUser(testUser1.getKeycloakUserId(), testBook1.getBookId());
        assertTrue(result.isPresent());
        assertEquals(testList1.getReadingListId(), result.get().getReadingListId());
    }

    @Test
    @Transactional
    void shouldNotFindListForBookInAnotherUsersList() {
        repository.addBookToReadingList(testList1.getReadingListId(), testBook1.getBookId());
        User otherUser = UserTestUtils.createValidUser();
        Optional<ReadingList> result = repository.findReadingListContainingBookForUser(otherUser.getKeycloakUserId(), testBook1.getBookId());
        assertFalse(result.isPresent());
    }
}