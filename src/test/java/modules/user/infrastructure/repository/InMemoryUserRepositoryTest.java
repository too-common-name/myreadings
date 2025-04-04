package modules.user.infrastructure.repository;

import modules.user.domain.UiTheme;
import modules.user.domain.User;
import modules.user.domain.UserImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryUserRepositoryTest {

    private InMemoryUserRepository userRepository;

    @BeforeEach
    public void setUp() {
        userRepository = new InMemoryUserRepository();
    }

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
        assertEquals(user, savedUser);

        Optional<User> foundUser = userRepository.findById(keycloakUserId);
        assertTrue(foundUser.isPresent());
        assertEquals(user, foundUser.get());
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
        assertTrue(allUsers.contains(user1));
        assertTrue(allUsers.contains(user2));
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