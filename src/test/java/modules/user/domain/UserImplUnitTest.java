package modules.user.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import modules.user.core.domain.UiTheme;
import modules.user.core.domain.User;
import modules.user.core.domain.UserImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class UserImplUnitTest {

    private static Validator validator;

    @BeforeAll
    public static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void createUserWithValidDataSuccessful() {
        User user = createValidUserBuilder().build();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Validation should pass for valid user data");
    }

    @ParameterizedTest(name = "Validation should fail for {0} with invalid value")
    @MethodSource("provideInvalidUserArguments")
    void userValidationFailsForInvalidFields(String fieldName, Object invalidValue) {
        UserImpl.UserImplBuilder builder = createValidUserBuilder();

        switch (fieldName) {
            case "firstName": builder.firstName((String) invalidValue); break;
            case "lastName": builder.lastName((String) invalidValue); break;
            case "username": builder.username((String) invalidValue); break;
            case "email": builder.email((String) invalidValue); break;
            default: fail("Test case for field " + fieldName + " not implemented.");
        }

        User user = builder.build();
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty(), "Validation should fail for " + fieldName);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals(fieldName)),
                "Validation error should be on field '" + fieldName + "'");
    }

    private static Stream<Arguments> provideInvalidUserArguments() {
        String longString150 = ".".repeat(150);
        return Stream.of(
                Arguments.of("firstName", longString150),
                Arguments.of("lastName", " "),
                Arguments.of("lastName", longString150),
                Arguments.of("username", ""),
                Arguments.of("username", longString150),
                Arguments.of("email", " "),
                Arguments.of("email", "invalid-email-format"),
                Arguments.of("email", longString150)
        );
    }

    @Test
    void testUpdateUiTheme() {
        UserImpl user = (UserImpl) createValidUserBuilder().build();
        user.setThemePreference(UiTheme.DARK);
        assertEquals(UiTheme.DARK, user.getThemePreference());
    }

    private UserImpl.UserImplBuilder createValidUserBuilder() {
        return UserImpl.builder()
                .keycloakUserId(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .email("john.doe@example.com")
                .themePreference(UiTheme.LIGHT);
    }
}