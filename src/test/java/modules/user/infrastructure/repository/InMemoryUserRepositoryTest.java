package modules.user.infrastructure.repository;

import common.InMemoryRepositoryTestProfile;
import io.quarkus.test.junit.TestProfile;
import modules.user.core.usecases.repositories.UserRepository;
import modules.user.infrastructure.persistence.in_memory.InMemoryUserRepository;

@TestProfile(InMemoryRepositoryTestProfile.class)
public class InMemoryUserRepositoryTest extends AbstractUserRepositoryTest {

    @Override
    protected UserRepository getRepository() {
        return new InMemoryUserRepository();
    }
}