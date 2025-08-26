package org.modular.playground.user.infrastructure.repository;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;

import org.modular.playground.common.JpaRepositoryTestProfile;
import org.modular.playground.user.core.usecases.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;

@QuarkusTest
@TestTransaction
@TestProfile(JpaRepositoryTestProfile.class)
public class JpaUserRepositoryTest extends AbstractUserRepositoryTest {

    @Inject
    UserRepository jpaRepository;

    @Override
    protected UserRepository getRepository() {
        return jpaRepository;
    }

    @BeforeEach
    @Override
    void setUp() {
        super.setUp();
    }
}