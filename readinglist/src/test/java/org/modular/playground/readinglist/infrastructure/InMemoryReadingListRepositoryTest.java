package org.modular.playground.readinglist.infrastructure;

import io.quarkus.test.junit.TestProfile;
import org.modular.playground.catalog.core.domain.Book;
import org.modular.playground.catalog.utils.CatalogTestUtils;
import org.modular.playground.common.InMemoryRepositoryTestProfile;
import org.modular.playground.readinglist.core.usecases.repositories.ReadingListRepository;
import org.modular.playground.readinglist.infrastructure.persistence.in_memory.InMemoryReadingListRepository;
import org.modular.playground.user.core.domain.User;
import org.modular.playground.user.utils.UserTestUtils;
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