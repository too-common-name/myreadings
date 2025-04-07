package modules.user.domain;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import modules.user.core.domain.UiTheme;
import modules.user.core.domain.User;
import modules.user.core.domain.UserImpl;
import jakarta.validation.ConstraintViolation;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.UUID;

public class UserImplUnitTest {

    private static Validator validator;

    @BeforeAll
    public static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void createUserWithValidDataSuccessful() {
        User user = UserImpl.builder()
                .keycloakUserId(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .email("john.doe@example.com")
                .themePreference(UiTheme.LIGHT)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Validation should pass for valid user data");
    }

    @Test
    void createUserWithTooLongFirstNameFailsValidation() {
        String longFirstName = "VeryLongFirstNameExceedingFiftyCharactersForSure";
        User user = UserImpl.builder()
                .keycloakUserId(UUID.randomUUID())
                .firstName(longFirstName)
                .lastName("Doe")
                .username("johndoe")
                .email("john.doe@example.com")
                .themePreference(UiTheme.LIGHT)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Validation should fail for too long firstName");
    }

    @Test
    void createUserWithBlankLastNameFailsValidation() {
        User user = UserImpl.builder()
                .keycloakUserId(UUID.randomUUID())
                .firstName("John")
                .lastName(" ")
                .username("johndoe")
                .email("john.doe@example.com")
                .themePreference(UiTheme.LIGHT)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Validation should fail for blank lastName");
    }

    @Test
    void createUserWithTooLongLastNameFailsValidation() {
        String longLastName = "VeryLongLastNameExceedingFiftyCharactersForSure";
        User user = UserImpl.builder()
                .keycloakUserId(UUID.randomUUID())
                .firstName("John")
                .lastName(longLastName)
                .username("johndoe")
                .email("john.doe@example.com")
                .themePreference(UiTheme.LIGHT)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Validation should fail for too long lastName");
    }

    @Test
    void createUserWithBlankUsernameFailsValidation() {
        User user = UserImpl.builder()
                .keycloakUserId(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .username("")
                .email("john.doe@example.com")
                .themePreference(UiTheme.LIGHT)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Validation should fail for blank username");
    }

    @Test
    void createUserWithTooLongUsernameFailsValidation() {
        String longUsername = "VeryLongUsernameExceedingFiftyCharactersForSure";
        User user = UserImpl.builder()
                .keycloakUserId(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .username(longUsername)
                .email("john.doe@example.com")
                .themePreference(UiTheme.LIGHT)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Validation should fail for too long username");
    }

    @Test
    void createUserWithBlankEmailFailsValidation() {
        User user = UserImpl.builder()
                .keycloakUserId(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .email("   ") // Blank email
                .themePreference(UiTheme.LIGHT)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Validation should fail for blank email");
    }

    @Test
    void createUserWithInvalidEmailFormatFailsValidation() {
        User user = UserImpl.builder()
                .keycloakUserId(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .email("invalid-email-format") // Invalid email format
                .themePreference(UiTheme.LIGHT)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Validation should fail for invalid email format");
    }

    @Test
    void testUpdateUiTheme() {
        UserImpl user = (UserImpl) UserImpl.builder()
                .keycloakUserId(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .email("john.doe@example.com")
                .themePreference(UiTheme.LIGHT)
                .build();

        user.setThemePreference(UiTheme.DARK);
        assertEquals(UiTheme.DARK, user.getThemePreference());
    }
}