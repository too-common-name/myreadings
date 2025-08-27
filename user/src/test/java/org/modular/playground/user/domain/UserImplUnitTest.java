package org.modular.playground.user.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.modular.playground.user.core.domain.UiTheme;
import org.modular.playground.user.core.domain.User;
import org.modular.playground.user.core.domain.UserImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.annotation.Annotation;
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
    void shouldCreateUserSuccessfullyWhenDataIsValid() {
        User user = createValidUserBuilder().build();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest(name = "Validation for {0} should fail with {2}")
    @MethodSource("provideInvalidUserArguments")
    void shouldFailValidationWhenUserFieldIsInvalid(String fieldName, Object invalidValue, Class<? extends Annotation> expectedViolation) {
        UserImpl.UserImplBuilder builder = createBaseUserBuilder();

        if (!fieldName.equals("firstName")) builder.firstName("John");
        if (!fieldName.equals("lastName")) builder.lastName("Doe");
        if (!fieldName.equals("username")) builder.username("johndoe");
        if (!fieldName.equals("email")) builder.email("john.doe@example.com");

        switch (fieldName) {
            case "firstName": builder.firstName((String) invalidValue); break;
            case "lastName": builder.lastName((String) invalidValue); break;
            case "username": builder.username((String) invalidValue); break;
            case "email": builder.email((String) invalidValue); break;
            default: fail("Test case for field " + fieldName + " not implemented.");
        }

        User user = builder.build();
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals(fieldName) &&
                v.getConstraintDescriptor().getAnnotation().annotationType().equals(expectedViolation)
        ));
    }

    @Test
    void shouldApplyDefaultThemePreference() {
        User user = createBaseUserBuilder()
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .email("john.doe@example.com")
                .build();

        assertEquals(UiTheme.LIGHT, user.getThemePreference());
    }

    @Test
    void shouldRespectEqualsAndHashCodeContracts() {
        UUID userId = UUID.randomUUID();
        User user1 = createBaseUserBuilder()
                .keycloakUserId(userId)
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .email("john.doe@example.com")
                .build();

        User user2 = createBaseUserBuilder()
                .keycloakUserId(userId)
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .email("john.doe@example.com")
                .build();

        User user3 = createBaseUserBuilder()
                .keycloakUserId(UUID.randomUUID())
                .firstName("Jane")
                .build();
        
        assertEquals(user1, user2, "Users with the same ID should be equal");
        assertEquals(user1.hashCode(), user2.hashCode(), "Hash codes for equal objects should be the same");

        assertNotEquals(user1, user3, "Users with different IDs should not be equal");
        assertNotEquals(user1.hashCode(), user3.hashCode(), "Hash codes for non-equal objects should ideally be different");
        
        assertNotEquals(user1, null);
        assertNotEquals(user1, new Object());
    }

    @Test
    void shouldGenerateValidToString() {
        User user = createValidUserBuilder().build();
        String userString = user.toString();

        assertNotNull(userString);
        assertTrue(userString.contains("keycloakUserId"));
        assertTrue(userString.contains("johndoe"));
    }

    private static Stream<Arguments> provideInvalidUserArguments() {
        return Stream.of(
                Arguments.of("firstName", " ", NotBlank.class),
                Arguments.of("firstName", ".".repeat(51), Size.class),
                Arguments.of("lastName", " ", NotBlank.class),
                Arguments.of("lastName", ".".repeat(51), Size.class),
                Arguments.of("username", "", NotBlank.class),
                Arguments.of("username", ".".repeat(81), Size.class),
                Arguments.of("email", " ", NotBlank.class),
                Arguments.of("email", "invalid-email-format", Email.class)
        );
    }
    
    private UserImpl.UserImplBuilder createBaseUserBuilder() {
        return UserImpl.builder()
                .keycloakUserId(UUID.randomUUID())
                .themePreference(UiTheme.LIGHT);
    }
    
    private UserImpl.UserImplBuilder createValidUserBuilder() {
        return createBaseUserBuilder()
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .email("john.doe@example.com");
    }
}