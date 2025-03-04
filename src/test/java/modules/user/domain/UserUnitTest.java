package modules.user.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.UUID;

public class UserUnitTest {

    @Test
    void createUserSuccessful() {
        UUID userId = UUID.randomUUID();
        String firstName = "John";
        String lastName = "Doe";
        String username = "johndoe";
        String email = "john.doe@example.com";
        UiTheme initialTheme = UiTheme.LIGHT;

        User user = new User.UserBuilder()
                .userId(userId)
                .firstName(firstName)
                .lastName(lastName)
                .username(username)
                .email(email)
                .themePreference(initialTheme)
                .build();

        assertNotNull(user);
        assertEquals(userId, user.getUserId());
        assertEquals(firstName, user.getFirstName());
        assertEquals(lastName, user.getLastName());
        assertEquals(username, user.getUsername());
        assertEquals(email, user.getEmail());
        assertEquals(initialTheme, user.getThemePreference());
    }

    @Test
    void testUpdateUiTheme() {
        User user = new User.UserBuilder()
                .userId(UUID.randomUUID())
                .firstName("Jane")
                .lastName("Smith")
                .username("janesmith")
                .email("jane.smith@example.com")
                .themePreference(UiTheme.LIGHT)
                .build();

        UiTheme newTheme = UiTheme.DARK;

        user.updateUiTheme(newTheme);

        assertEquals(newTheme, user.getThemePreference());
    }

    @Test
    void createUser_withoutThemePreference_shouldDefaultToLight() {
        UUID userId = UUID.randomUUID();
        String username = "testuser";
        String email = "test@example.com";

        User user = new User.UserBuilder()
                .userId(userId)
                .username(username)
                .email(email)
                .build();

        assertEquals(UiTheme.LIGHT, user.getThemePreference());
    }
}