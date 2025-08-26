package org.modular.playground.catalog.infrastructure;

import io.quarkus.test.junit.TestProfile;
import org.modular.playground.catalog.core.usecases.repositories.BookRepository;
import org.modular.playground.catalog.infrastructure.persistence.in_memory.InMemoryBookRepository;
import org.modular.playground.common.InMemoryRepositoryTestProfile;

@TestProfile(InMemoryRepositoryTestProfile.class)
public class InMemoryBookRepositoryTest extends AbstractBookRepositoryTest {

    @Override
    protected BookRepository getRepository() {
        return new InMemoryBookRepository();
    }
}