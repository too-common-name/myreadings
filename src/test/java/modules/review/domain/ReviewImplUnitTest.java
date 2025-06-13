package modules.review.domain;

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

import modules.catalog.core.domain.Book;
import modules.catalog.core.domain.BookImpl;
import modules.review.core.domain.Review;
import modules.review.core.domain.ReviewImpl;
import modules.user.core.domain.UiTheme;
import modules.user.core.domain.User;
import modules.user.core.domain.UserImpl;

public class ReviewImplUnitTest {

    private static Validator validator;

    @BeforeAll
    public static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void createReviewWithValidDataSuccessful() {
        Review review = ReviewImpl.builder()
                .reviewId(UUID.randomUUID())
                .book(createValidBook())
                .user(createValidUser())
                .reviewText("This is a valid review text within the 200 character limit.")
                .rating(3)
                .publicationDate(LocalDateTime.now())
                .build();

        Set<ConstraintViolation<Review>> violations = validator.validate(review);
        assertTrue(violations.isEmpty(), "No validation errors expected for valid Review");
        assertNotNull(review);
    }

    @Test
    void createReviewWithoutBookFailsValidation() {
        Review review = ReviewImpl.builder()
                .reviewId(UUID.randomUUID())
                .book(null) // Book is null, should fail @NotNull
                .user(createValidUser())
                .reviewText("Test review text")
                .rating(3)
                .publicationDate(LocalDateTime.now())
                .build();

        Set<ConstraintViolation<Review>> violations = validator.validate(review);
        assertFalse(violations.isEmpty(), "Validation should fail for missing book");
        assertTrue(violations.stream().anyMatch(violation -> violation.getPropertyPath().toString().equals("book")),
                "Validation error should be on field 'book'");
    }

    @Test
    void createReviewWithoutUserFailsValidation() {
        Review review = ReviewImpl.builder()
                .reviewId(UUID.randomUUID())
                .book(createValidBook())
                .user(null) // User is null, should fail @NotNull
                .reviewText("Test review text")
                .rating(3)
                .publicationDate(LocalDateTime.now())
                .build();

        Set<ConstraintViolation<Review>> violations = validator.validate(review);
        assertFalse(violations.isEmpty(), "Validation should fail for missing user");
        assertTrue(violations.stream().anyMatch(violation -> violation.getPropertyPath().toString().equals("user")),
                "Validation error should be on field 'user'");
    }

    @Test
    void createReviewWithTooLongReviewTextFailsValidation() {
        String longReviewText = "This review text is intentionally made very long to exceed the 200 character limit imposed by the @Size annotation. We need to make sure that validation correctly identifies this as an invalid review text because it is too long....................................................................................................................."; // Exceeds 200 chars

        Review review = ReviewImpl.builder()
                .reviewId(UUID.randomUUID())
                .book(createValidBook())
                .user(createValidUser())
                .reviewText(longReviewText) // reviewText is too long, should fail @Size
                .rating(3)
                .publicationDate(LocalDateTime.now())
                .build();

        Set<ConstraintViolation<Review>> violations = validator.validate(review);
        assertFalse(violations.isEmpty(), "Validation should fail for too long reviewText");
        assertTrue(violations.stream().anyMatch(violation -> violation.getPropertyPath().toString().equals("reviewText")),
                "Validation error should be on field 'reviewText'");
    }

    @Test
    void createReviewWithRatingTooLowFailsValidation() {
        Review review = ReviewImpl.builder()
                .reviewId(UUID.randomUUID())
                .book(createValidBook())
                .user(createValidUser())
                .reviewText("Test review text")
                .rating(0) // Rating too low, should fail @Min
                .publicationDate(LocalDateTime.now())
                .build();

        Set<ConstraintViolation<Review>> violations = validator.validate(review);
        assertFalse(violations.isEmpty(), "Validation should fail for rating too low");
        assertTrue(violations.stream().anyMatch(violation -> violation.getPropertyPath().toString().equals("rating")),
                "Validation error should be on field 'rating'");
    }

    @Test
    void createReviewWithRatingTooHighFailsValidation() {
        Review review = ReviewImpl.builder()
                .reviewId(UUID.randomUUID())
                .book(createValidBook())
                .user(createValidUser())
                .reviewText("Test review text")
                .rating(6) // Rating too high, should fail @Max
                .publicationDate(LocalDateTime.now())
                .build();

        Set<ConstraintViolation<Review>> violations = validator.validate(review);
        assertFalse(violations.isEmpty(), "Validation should fail for rating too high");
        assertTrue(violations.stream().anyMatch(violation -> violation.getPropertyPath().toString().equals("rating")),
                "Validation error should be on field 'rating'");
    }

    // Helper methods to create valid Book and User instances for tests
    private Book createValidBook() {
        return BookImpl.builder()
                .bookId(UUID.randomUUID())
                .isbn("978-0321765723")
                .title("The катание Programming Language")
                .authors(java.util.Arrays.asList("Brian W. Kernighan", "Dennis M. Ritchie"))
                .publicationDate(LocalDateTime.now().toLocalDate().minusYears(40))
                .publisher("Prentice Hall")
                .description("The classic book on C programming.")
                .pageCount(272)
                .coverImageId("cover123")
                .originalLanguage("en")
                .build();
    }

    private User createValidUser() {
        return UserImpl.builder()
                .keycloakUserId(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .email("john.doe@example.com")
                .themePreference(UiTheme.LIGHT)
                .build();
    }
}