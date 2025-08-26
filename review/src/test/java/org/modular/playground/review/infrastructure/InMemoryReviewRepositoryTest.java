package org.modular.playground.review.infrastructure;

import io.quarkus.test.junit.TestProfile;
import org.modular.playground.catalog.core.domain.Book;
import org.modular.playground.catalog.utils.CatalogTestUtils;
import org.modular.playground.common.InMemoryRepositoryTestProfile;
import org.modular.playground.review.core.usecases.repositories.ReviewRepository;
import org.modular.playground.review.infrastructure.persistence.in_memory.InMemoryReviewRepository;
import org.modular.playground.user.core.domain.User;
import org.modular.playground.user.utils.UserTestUtils;
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