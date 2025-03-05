package modules.readinglist.domain;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import modules.user.domain.UserImpl;
import modules.readinglist.domain.ReadingListImpl.ReadingListBuilder;
import modules.user.domain.UiTheme;
import modules.user.domain.User;

public class ReadingListImplUnitTest {

    private static Validator validator;

    @BeforeAll
    public static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void createReadingListWithValidDataSuccessful() {
        ReadingList readingList = createValidReadingList();
        Set<ConstraintViolation<ReadingList>> violations = validator.validate(readingList);
        assertTrue(violations.isEmpty(), "No validation errors expected for valid ReadingList");
    }

    @Test
    void createReadingListWithoutUserFailsValidation() {
        ReadingList readingList = createValidReadingListBuilder().user(null).build();
        Set<ConstraintViolation<ReadingList>> violations = validator.validate(readingList);
        assertFalse(violations.isEmpty(), "Validation should fail for missing user");
    }

    @Test
    void createReadingListWithBlankNameFailsValidation() {
        ReadingList readingList = createValidReadingListBuilder().name("").build();
        Set<ConstraintViolation<ReadingList>> violations = validator.validate(readingList);
        assertFalse(violations.isEmpty(), "Validation should fail for blank name");
    }

    @Test
    void createReadingListWithTooLongNameFailsValidation() {
        String longName = "This reading list name is too long and exceeds thirty characters";
        ReadingList readingList = createValidReadingListBuilder().name(longName).build();
        Set<ConstraintViolation<ReadingList>> violations = validator.validate(readingList);
        assertFalse(violations.isEmpty(), "Validation should fail for too long name");
    }

    @Test
    void createReadingListWithTooLongDescriptionFailsValidation() {
        String longDescription = "This description is intentionally made very long to exceed the 200 character limit imposed by the @Size annotation. We need to make sure that validation correctly identifies this as an invalid description because it is too long.....................................................................................................................";
        ReadingList readingList = createValidReadingListBuilder().description(longDescription).build();
        Set<ConstraintViolation<ReadingList>> violations = validator.validate(readingList);
        assertFalse(violations.isEmpty(), "Validation should fail for too long description");
    }


    private ReadingList createValidReadingList() {
        return createValidReadingListBuilder().build();
    }

    private ReadingListBuilder createValidReadingListBuilder() {
        return new ReadingListImpl.ReadingListBuilder()
                .readingListId(UUID.randomUUID())
                .user(createValidUser())
                .name("My Reading List")
                .description("This is a valid description within the limit")
                .creationDate(LocalDateTime.now());
    }

    private User createValidUser() {
        return new UserImpl.UserBuilder()
                .userId(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .email("john.doe@example.com")
                .themePreference(UiTheme.LIGHT)
                .build();
    }
}