package modules.readinglist.infrastructure;

import common.InMemoryRepositoryTestProfile;
import io.quarkus.test.junit.TestProfile;
import modules.catalog.core.domain.Book;
import modules.catalog.utils.CatalogTestUtils;
import modules.readinglist.core.usecases.repositories.ReadingListRepository;
import modules.readinglist.infrastructure.persistence.in_memory.InMemoryReadingListRepository;
import modules.user.core.domain.User;
import modules.user.utils.UserTestUtils;
import org.junit.jupiter.api.BeforeEach;

@TestProfile(InMemoryRepositoryTestProfile.class)
public class InMemoryReadingListRepositoryTest extends AbstractReadingListRepositoryTest {
    
    private InMemoryReadingListRepository inMemoryRepo;
    
    @BeforeEach
    void setupRepo() {
        inMemoryRepo = new InMemoryReadingListRepository();
    }
    
    @Override
    protected ReadingListRepository getRepository() {
        return inMemoryRepo;
    }

    @Override
    protected User createAndSaveUser() {
        return UserTestUtils.createValidUser();
    }

    @Override
    protected Book createAndSaveBook() {
        return CatalogTestUtils.createValidBook();
    }
}