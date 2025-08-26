package org.modular.playground.user.infrastructure.repository;

import io.quarkus.test.junit.TestProfile;

import org.modular.playground.common.InMemoryRepositoryTestProfile;
import org.modular.playground.user.core.usecases.repositories.UserRepository;
import org.modular.playground.user.infrastructure.persistence.in_memory.InMemoryUserRepository;

@TestProfile(InMemoryRepositoryTestProfile.class)
public class InMemoryUserRepositoryTest extends AbstractUserRepositoryTest {

    @Override
    protected UserRepository getRepository() {
        return new InMemoryUserRepository();
    }
}