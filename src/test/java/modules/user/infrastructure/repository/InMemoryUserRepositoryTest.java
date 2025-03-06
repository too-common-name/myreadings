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
        UUID userId = UUID.randomUUID();
        User user = new UserImpl.UserBuilder()
                .userId(userId)
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .email("john.doe@example.com")
                .themePreference(UiTheme.LIGHT)
                .build();

        User savedUser = userRepository.save(user);
        assertEquals(user, savedUser);

        Optional<User> foundUser = userRepository.findById(userId);
        assertTrue(foundUser.isPresent());
        assertEquals(user, foundUser.get());
    }

    @Test
    public void testFindByIdFails() {
        UUID userId = UUID.randomUUID();
        Optional<User> foundUser = userRepository.findById(userId);
        assertFalse(foundUser.isPresent());
    }

    @Test
    public void testFindAll() {
        User user1 = new UserImpl.UserBuilder()
                .userId(UUID.randomUUID())
                .firstName("Alice")
                .lastName("Smith")
                .username("alicesmith")
                .email("alice.smith@example.com")
                .themePreference(UiTheme.DARK)
                .build();
        User user2 = new UserImpl.UserBuilder()
                .userId(UUID.randomUUID())
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
        UUID userId = UUID.randomUUID();
        User user = new UserImpl.UserBuilder()
                .userId(userId)
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .email("john.doe@example.com")
                .themePreference(UiTheme.LIGHT)
                .build();

        userRepository.save(user);
        userRepository.deleteById(userId);

        Optional<User> foundUser = userRepository.findById(userId);
        assertFalse(foundUser.isPresent());
    }

    @Test
    public void testDeleteByIdNotFound() {
        UUID userId = UUID.randomUUID();
        assertDoesNotThrow(() -> userRepository.deleteById(userId));
    }
}