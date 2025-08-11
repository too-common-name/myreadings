package modules.review.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import modules.catalog.core.domain.Book;
import modules.review.core.domain.Review;
import modules.review.core.domain.ReviewImpl;
import modules.user.core.domain.User;
import modules.user.utils.UserTestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import modules.catalog.utils.CatalogTestUtils;

import java.lang.annotation.Annotation;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ReviewUnitTest {

    private static Validator validator;

    @BeforeAll
    public static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldCreateReviewSuccessfullyWhenDataIsValid() {
        Review review = createValidReviewBuilder().build();
        Set<ConstraintViolation<Review>> violations = validator.validate(review);
        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest(name = "Validation for {0} should fail with {2}")
    @MethodSource("provideInvalidArguments")
    void shouldFailValidationForInvalidField(String fieldName, Object invalidValue, Class<? extends Annotation> expectedViolation) {
        ReviewImpl.ReviewImplBuilder builder = createValidReviewBuilder();

        switch (fieldName) {
            case "book":
                builder.book((Book) invalidValue);
                break;
            case "user":
                builder.user((User) invalidValue);
                break;
            case "reviewText":
                builder.reviewText((String) invalidValue);
                break;
            case "rating":
                builder.rating((Integer) invalidValue);
                break;
            default:
                fail("Test case for field " + fieldName + " not implemented.");
        }

        Review review = builder.build();
        Set<ConstraintViolation<Review>> violations = validator.validate(review);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals(fieldName) &&
                v.getConstraintDescriptor().getAnnotation().annotationType().equals(expectedViolation)
        ));
    }

    private static Stream<Arguments> provideInvalidArguments() {
        return Stream.of(
            Arguments.of("book", null, NotNull.class),
            Arguments.of("user", null, NotNull.class),
            Arguments.of("reviewText", ".".repeat(201), Size.class),
            Arguments.of("rating", 0, Min.class),
            Arguments.of("rating", 6, Max.class)
        );
    }

    private ReviewImpl.ReviewImplBuilder createValidReviewBuilder() {
        return ReviewImpl.builder()
                .book(CatalogTestUtils.createValidBook())
                .user(UserTestUtils.createValidUser())
                .reviewText("This is a valid review text.")
                .rating(4)
                .publicationDate(LocalDateTime.now());
    }
}