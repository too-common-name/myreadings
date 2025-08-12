package modules.readinglist.infrastructure;

import modules.catalog.core.domain.Book;
import modules.readinglist.core.domain.ReadingList;
import modules.readinglist.core.usecases.repositories.ReadingListRepository;
import modules.readinglist.utils.ReadingListTestUtils;
import modules.user.core.domain.User;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractReadingListRepositoryTest {

    protected abstract ReadingListRepository getRepository();
    protected abstract User createAndSaveUser();
    protected abstract Book createAndSaveBook();

    protected void runTransactionalStep(Runnable step) {
        step.run();
    }
    protected <T> T runTransactionalStep(Supplier<T> step) {
        return step.get();
    }

    @Test
    void shouldCreateAndFindById() {
        User user = createAndSaveUser();
        runTransactionalStep(() -> {
            ReadingList newList = ReadingListTestUtils.createValidReadingListForUser(user, "New List");
            ReadingList saved = getRepository().create(newList);
            Optional<ReadingList> found = getRepository().findById(saved.getReadingListId());
            assertTrue(found.isPresent());
            assertEquals(saved.getReadingListId(), found.get().getReadingListId());
        });
    }

    @Test
    void shouldDeleteById() {
        User user = createAndSaveUser();
        ReadingList list = runTransactionalStep(() -> 
            getRepository().create(ReadingListTestUtils.createValidReadingListForUser(user, "To Delete"))
        );
        runTransactionalStep(() -> {
            getRepository().deleteById(list.getReadingListId());
            Optional<ReadingList> found = getRepository().findById(list.getReadingListId());
            assertFalse(found.isPresent());
        });
    }
    
    @Test
    void shouldUpdateReadingList() {
        User user = createAndSaveUser();
        ReadingList originalList = runTransactionalStep(() -> 
            getRepository().create(ReadingListTestUtils.createValidReadingListForUser(user, "Original Name"))
        );
        runTransactionalStep(() -> {
            ReadingList toUpdate = ReadingListTestUtils.from(originalList).name("Updated Name").build();
            ReadingList updated = getRepository().update(toUpdate);
            assertEquals("Updated Name", updated.getName());
        });
    }

    @Test
    void shouldAddAndRemoveBook() {
        User user = createAndSaveUser();
        Book book = createAndSaveBook();
        ReadingList list = runTransactionalStep(() -> 
            getRepository().create(ReadingListTestUtils.createValidReadingListForUser(user, "List With Books"))
        );
        runTransactionalStep(() -> {
            getRepository().addBookToReadingList(list.getReadingListId(), book.getBookId());
            List<UUID> bookIds = getRepository().getBookIdsInReadingList(list.getReadingListId());
            assertEquals(1, bookIds.size());
        });
        runTransactionalStep(() -> {
            getRepository().removeBookFromReadingList(list.getReadingListId(), book.getBookId());
            List<UUID> bookIds = getRepository().getBookIdsInReadingList(list.getReadingListId());
            assertTrue(bookIds.isEmpty());
        });
    }

    @Test
    void shouldFindByUserId() {
        User user = createAndSaveUser();
        runTransactionalStep(() -> {
            getRepository().create(ReadingListTestUtils.createValidReadingListForUser(user, "List 1"));
            getRepository().create(ReadingListTestUtils.createValidReadingListForUser(user, "List 2"));
        });
        
        List<ReadingList> results = runTransactionalStep(() -> getRepository().findByUserId(user.getKeycloakUserId()));
        assertEquals(2, results.size());
    }
    
    @Test
    void shouldFindReadingListContainingBookForUser() {
        User user = createAndSaveUser();
        Book book = createAndSaveBook();
        ReadingList list = runTransactionalStep(() -> 
            getRepository().create(ReadingListTestUtils.createValidReadingListForUser(user, "My List"))
        );
        runTransactionalStep(() -> 
            getRepository().addBookToReadingList(list.getReadingListId(), book.getBookId())
        );

        Optional<ReadingList> result = runTransactionalStep(() -> 
            getRepository().findReadingListContainingBookForUser(user.getKeycloakUserId(), book.getBookId())
        );
        assertTrue(result.isPresent());
        assertEquals(list.getReadingListId(), result.get().getReadingListId());
    }

    @Test
    void shouldNotFindListForBookInAnotherUsersList() {
        User user1 = createAndSaveUser();
        User user2 = createAndSaveUser();
        Book book = createAndSaveBook();
        ReadingList list = runTransactionalStep(() -> 
            getRepository().create(ReadingListTestUtils.createValidReadingListForUser(user1, "User1's List"))
        );
        runTransactionalStep(() -> 
            getRepository().addBookToReadingList(list.getReadingListId(), book.getBookId())
        );
        
        Optional<ReadingList> result = runTransactionalStep(() -> 
            getRepository().findReadingListContainingBookForUser(user2.getKeycloakUserId(), book.getBookId())
        );
        assertFalse(result.isPresent());
    }
}