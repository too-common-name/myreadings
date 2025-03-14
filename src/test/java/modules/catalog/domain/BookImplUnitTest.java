package modules.catalog.domain;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

public class BookImplUnitTest {

    private static Validator validator;

    @BeforeAll
    public static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void createBookWithValidDataSuccessful() {
        Book book = createValidBookBuilder().build();
        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertTrue(violations.isEmpty(), "No validation errors expected for valid Book");
    }

    @Test
    void createBookWithBlankIsbnFailsValidation() {
        Book book = createValidBookBuilder().isbn("").build();
        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertFalse(violations.isEmpty(), "Validation should fail for blank isbn");
        assertTrue(violations.stream().anyMatch(violation -> violation.getPropertyPath().toString().equals("isbn")),
                "Validation error should be on field 'isbn'");
    }

    @Test
    void createBookWithBlankTitleFailsValidation() {
        Book book = createValidBookBuilder().title("").build();
        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertFalse(violations.isEmpty(), "Validation should fail for blank title");
        assertTrue(violations.stream().anyMatch(violation -> violation.getPropertyPath().toString().equals("title")),
                "Validation error should be on field 'title'");
    }

    @Test
    void createBookWithTooLongTitleFailsValidation() {
        String longTitle = "VeryLongTitleExceedingTwoHundredFiftyFiveCharactersForSureThisIsJustToTestTheMaximumLengthOfTheTitleFieldAndItShouldDefinitelyFailValidationBecauseItIsWayTooLongAndExceedsTheLimit..................................................................................................................................................................................................................";
        Book book = createValidBookBuilder().title(longTitle).build();
        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertFalse(violations.isEmpty(), "Validation should fail for too long title");
        assertTrue(violations.stream().anyMatch(violation -> violation.getPropertyPath().toString().equals("title")),
                "Validation error should be on field 'title'");
    }

    @Test
    void createBookWithPageCountNegativeFailsValidation() {
        Book book = createValidBookBuilder().pageCount(-1).build();
        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertFalse(violations.isEmpty(), "Validation should fail for negative pageCount");
        assertTrue(violations.stream().anyMatch(violation -> violation.getPropertyPath().toString().equals("pageCount")),
                "Validation error should be on field 'pageCount'");
    }

    @Test
    void createBookWithFuturePublicationDateFailsValidation() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        Book book = createValidBookBuilder().publicationDate(futureDate).build();
        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertFalse(violations.isEmpty(), "Validation should fail for future publicationDate");
        assertTrue(violations.stream().anyMatch(violation -> violation.getPropertyPath().toString().equals("publicationDate")),
                "Validation error should be on field 'publicationDate'");
    }

    @Test
    void createBookWithTooLongDescriptionFailsValidation() {
        String longDescription = "This description is intentionally made very long to exceed the 500 character limit imposed by the @Size annotation. We need to make sure that validation correctly identifies this as an invalid description because it is too long....................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................";
        Book book = createValidBookBuilder().description(longDescription).build();
        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertFalse(violations.isEmpty(), "Validation should fail for too long description");
        assertTrue(violations.stream().anyMatch(violation -> violation.getPropertyPath().toString().equals("description")),
                "Validation error should be on field 'description'");
    }

    @Test
    void createBookWithTooLongPublisherFailsValidation() {
        String longPublisher = "VeryLongPublisherNameExceedingTwoHundredFiftyFiveCharactersForSureThisIsJustToTestTheMaximumLengthOfThePublisherFieldAndItShouldDefinitelyFailValidationBecauseItIsWayTooLongAndExceedsTheLimit..................................................................................................................................................................................................................";
        Book book = createValidBookBuilder().publisher(longPublisher).build();
        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertFalse(violations.isEmpty(), "Validation should fail for too long publisher");
        assertTrue(violations.stream().anyMatch(violation -> violation.getPropertyPath().toString().equals("publisher")),
                "Validation error should be on field 'publisher'");
    }

    @Test
    void createBookWithTooLongCoverImageIdFailsValidation() {
        String longCoverImageId = "VeryLongCoverImageIdExceedingTwoHundredFiftyFiveCharactersForSureThisIsJustToTestTheMaximumLengthOfTheCoverImageIdFieldAndItShouldDefinitelyFailValidationBecauseItIsWayTooLongAndExceedsTheLimit..................................................................................................................................................................................................................";
        Book book = createValidBookBuilder().coverImageId(longCoverImageId).build();
        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertFalse(violations.isEmpty(), "Validation should fail for too long coverImageId");
        assertTrue(violations.stream().anyMatch(violation -> violation.getPropertyPath().toString().equals("coverImageId")),
                "Validation error should be on field 'coverImageId'");
    }

    @Test
    void createBookWithTooLongOriginalLanguageFailsValidation() {
        String longOriginalLanguage = "VeryLongOriginalLanguageNameExceedingFiftyCharactersForSureThisIsJustToTestTheMaximumLengthOfTheOriginalLanguageFieldAndItShouldDefinitelyFailValidationBecauseItIsWayTooLongAndExceedsTheLimit........................................................................................................................................................................................";
        Book book = createValidBookBuilder().originalLanguage(longOriginalLanguage).build();
        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertFalse(violations.isEmpty(), "Validation should fail for too long originalLanguage");
        assertTrue(violations.stream().anyMatch(violation -> violation.getPropertyPath().toString().equals("originalLanguage")),
                "Validation error should be on field 'originalLanguage'");
    }

    private BookImpl.BookBuilder createValidBookBuilder() {
        return new BookImpl.BookBuilder()
                .bookId(UUID.randomUUID())
                .isbn("978-0321765723")
                .title("Refactoring")
                .authors(Arrays.asList("Martin Fowler", "Kent Beck"))
                .publicationDate(LocalDate.now().minusYears(10))
                .publisher("Addison-Wesley")
                .description("Valid description")
                .pageCount(400)
                .coverImageId("cover123")
                .originalLanguage("en");
    }
}