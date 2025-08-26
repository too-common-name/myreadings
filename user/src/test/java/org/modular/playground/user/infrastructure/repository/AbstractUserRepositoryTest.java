package org.modular.playground.user.infrastructure.repository;

import org.modular.playground.user.core.domain.User;
import org.modular.playground.user.core.usecases.repositories.UserRepository;
import org.modular.playground.user.utils.UserTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractUserRepositoryTest {

    private UserRepository repository;

    protected abstract UserRepository getRepository();

    @BeforeEach
    void setUp() {
        this.repository = getRepository();
    }

    @Test
    void shouldSaveAndFindUserById() {
        User userToSave = UserTestUtils.createValidUser();
        repository.create(userToSave);

        Optional<User> foundUser = repository.findById(userToSave.getKeycloakUserId());

        assertTrue(foundUser.isPresent());
        assertEquals(userToSave.getKeycloakUserId(), foundUser.get().getKeycloakUserId());
        assertEquals(userToSave.getUsername(), foundUser.get().getUsername());
    }

    @Test
    void shouldReturnEmptyWhenFindingNonExistentUser() {
        Optional<User> foundUser = repository.findById(UUID.randomUUID());
        assertFalse(foundUser.isPresent());
    }

    @Test
    void shouldFindAllUsers() {
        User user1 = UserTestUtils.createValidUser();
        User user2 = UserTestUtils.createValidUser();
        repository.create(user1);
        repository.create(user2);

        List<User> allUsers = repository.findAll();

        assertEquals(2, allUsers.size());
        assertTrue(allUsers.stream().anyMatch(u -> u.getKeycloakUserId().equals(user1.getKeycloakUserId())));
        assertTrue(allUsers.stream().anyMatch(u -> u.getKeycloakUserId().equals(user2.getKeycloakUserId())));
    }

    @Test
    void shouldReturnEmptyListWhenNoUsersExist() {
        List<User> allUsers = repository.findAll();
        assertTrue(allUsers.isEmpty());
    }

    @Test
    void shouldDeleteUserById() {
        User userToDelete = UserTestUtils.createValidUser();
        repository.create(userToDelete);

        repository.deleteById(userToDelete.getKeycloakUserId());
        Optional<User> foundUser = repository.findById(userToDelete.getKeycloakUserId());

        assertFalse(foundUser.isPresent());
    }

    @Test
    void shouldNotThrowWhenDeletingNonExistentUser() {
        assertDoesNotThrow(() -> repository.deleteById(UUID.randomUUID()));
    }
}