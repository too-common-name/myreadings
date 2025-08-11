package modules.readinglist.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import modules.readinglist.core.domain.ReadingList;
import modules.readinglist.core.domain.ReadingListImpl;
import modules.user.core.domain.User;
import modules.user.utils.UserTestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.annotation.Annotation;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ReadingListImplUnitTest {

    private static Validator validator;

    @BeforeAll
    public static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void createReadingListWithValidDataSuccessful() {
        ReadingList readingList = createValidReadingListBuilder().build();
        Set<ConstraintViolation<ReadingList>> violations = validator.validate(readingList);
        assertTrue(violations.isEmpty(), "No validation errors expected for valid ReadingList");
    }

    @ParameterizedTest(name = "Validation for {0} should fail with {2}")
    @MethodSource("provideInvalidArguments")
    void readingListValidationFailsForInvalidFields(String fieldName, Object invalidValue, Class<? extends Annotation> expectedViolation) {
        ReadingListImpl.ReadingListImplBuilder builder = createValidReadingListBuilder();

        switch (fieldName) {
            case "user":
                builder.user((User) invalidValue);
                break;
            case "name":
                builder.name((String) invalidValue);
                break;
            case "description":
                builder.description((String) invalidValue);
                break;
            default:
                fail("Test case for field " + fieldName + " not implemented.");
        }

        ReadingList list = builder.build();
        Set<ConstraintViolation<ReadingList>> violations = validator.validate(list);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals(fieldName) &&
                v.getConstraintDescriptor().getAnnotation().annotationType().equals(expectedViolation)
        ));
    }

    private static Stream<Arguments> provideInvalidArguments() {
        return Stream.of(
            Arguments.of("user", null, NotNull.class),
            Arguments.of("name", " ", NotBlank.class),
            Arguments.of("name", ".".repeat(31), Size.class),
            Arguments.of("description", ".".repeat(201), Size.class)
        );
    }

    private ReadingListImpl.ReadingListImplBuilder createValidReadingListBuilder() {
        return ReadingListImpl.builder()
                .readingListId(UUID.randomUUID())
                .user(UserTestUtils.createValidUser())
                .books(new ArrayList<>())
                .name("My Reading List")
                .description("This is a valid description within the limit")
                .creationDate(LocalDateTime.now());
    }
}