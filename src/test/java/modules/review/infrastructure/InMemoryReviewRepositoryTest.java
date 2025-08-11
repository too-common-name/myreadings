package modules.review.infrastructure;

import common.InMemoryRepositoryTestProfile;
import io.quarkus.test.junit.TestProfile;
import modules.catalog.core.domain.Book;
import modules.catalog.utils.CatalogTestUtils;
import modules.review.core.usecases.repositories.ReviewRepository;
import modules.review.infrastructure.persistence.in_memory.InMemoryReviewRepository;
import modules.user.core.domain.User;
import modules.user.utils.UserTestUtils;
import org.junit.jupiter.api.BeforeEach;

@TestProfile(InMemoryRepositoryTestProfile.class)
public class InMemoryReviewRepositoryTest extends AbstractReviewRepositoryTest {
    
    private InMemoryReviewRepository inMemoryRepo;
    
    @BeforeEach
    void setupRepo() {
        inMemoryRepo = new InMemoryReviewRepository();
    }
    
    @Override
    protected ReviewRepository getRepository() {
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