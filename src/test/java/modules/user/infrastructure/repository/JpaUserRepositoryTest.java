package modules.user.infrastructure.repository;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import modules.user.core.domain.UiTheme;
import modules.user.core.domain.User;
import modules.user.core.domain.UserImpl;
import modules.user.core.usecases.repositories.UserRepository;

import org.junit.jupiter.api.Test;

import common.JpaRepositoryTestProfile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestTransaction
@TestProfile(JpaRepositoryTestProfile.class)
public class JpaUserRepositoryTest {

    @Inject
    UserRepository userRepository;

    @Test
    public void testSaveAndFindById() {
        UUID keycloakUserId = UUID.randomUUID();
        User user = UserImpl.builder()
                .keycloakUserId(keycloakUserId)
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .email("john.doe@example.com")
                .themePreference(UiTheme.LIGHT)
                .build();

        User savedUser = userRepository.save(user);
        assertNotNull(savedUser.getKeycloakUserId());
        assertEquals(user.getKeycloakUserId(), savedUser.getKeycloakUserId());

        Optional<User> foundUser = userRepository.findById(keycloakUserId);
        assertTrue(foundUser.isPresent());
        assertEquals(user.getKeycloakUserId(), foundUser.get().getKeycloakUserId());
        assertEquals(user.getFirstName(), foundUser.get().getFirstName());
        assertEquals(user.getLastName(), foundUser.get().getLastName());
        assertEquals(user.getUsername(), foundUser.get().getUsername());
        assertEquals(user.getEmail(), foundUser.get().getEmail());
        assertEquals(user.getThemePreference(), foundUser.get().getThemePreference());
    }

    @Test
    public void testFindByIdFails() {
        UUID keycloakUserId = UUID.randomUUID();
        Optional<User> foundUser = userRepository.findById(keycloakUserId);
        assertFalse(foundUser.isPresent());
    }

    @Test
    public void testFindAll() {
        User user1 = UserImpl.builder()
                .keycloakUserId(UUID.randomUUID())
                .firstName("Alice")
                .lastName("Smith")
                .username("alicesmith")
                .email("alice.smith@example.com")
                .themePreference(UiTheme.DARK)
                .build();
        User user2 = UserImpl.builder()
                .keycloakUserId(UUID.randomUUID())
                .firstName("Bob")
                .lastName("Johnson")
                .username("bobjohnson")
                .email("bob.johnson@example.com")
                .themePreference(UiTheme.LIGHT)
                .build();

        userRepository.save(user1);
        userRepository.save(user2);

        List<User> allUsers = userRepository.findAll();
        assertEquals(2, allUsers.size());
        assertTrue(allUsers.stream().anyMatch(u -> u.getKeycloakUserId().equals(user1.getKeycloakUserId())));
        assertTrue(allUsers.stream().anyMatch(u -> u.getKeycloakUserId().equals(user2.getKeycloakUserId())));
    }

    @Test
    public void testFindAllEmpty() {
        List<User> allUsers = userRepository.findAll();
        assertTrue(allUsers.isEmpty());
    }

    @Test
    public void testDeleteById() {
        UUID keycloakUserId = UUID.randomUUID();
        User user = UserImpl.builder()
                .keycloakUserId(keycloakUserId)
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .email("john.doe@example.com")
                .themePreference(UiTheme.LIGHT)
                .build();

        userRepository.save(user);
        userRepository.deleteById(keycloakUserId);

        Optional<User> foundUser = userRepository.findById(keycloakUserId);
        assertFalse(foundUser.isPresent());
    }

    @Test
    public void testDeleteByIdNotFound() {
        UUID keycloakUserId = UUID.randomUUID();
        assertDoesNotThrow(() -> userRepository.deleteById(keycloakUserId));
    }
}