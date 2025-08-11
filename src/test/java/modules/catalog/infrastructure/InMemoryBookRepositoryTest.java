package modules.catalog.infrastructure;

import common.InMemoryRepositoryTestProfile;
import io.quarkus.test.junit.TestProfile;
import modules.catalog.core.usecases.repositories.BookRepository;
import modules.catalog.infrastructure.persistence.in_memory.InMemoryBookRepository;

@TestProfile(InMemoryRepositoryTestProfile.class)
public class InMemoryBookRepositoryTest extends AbstractBookRepositoryTest {

    @Override
    protected BookRepository getRepository() {
        return new InMemoryBookRepository();
    }
}