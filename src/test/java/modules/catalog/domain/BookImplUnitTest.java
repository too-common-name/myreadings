package modules.catalog.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import modules.catalog.core.domain.Book;
import modules.catalog.core.domain.BookImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import modules.catalog.utils.CatalogTestUtils;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class BookImplUnitTest {

    private static Validator validator;

    @BeforeAll
    public static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldCreateBookSuccessfullyWhenDataIsValid() {
        Book book = CatalogTestUtils.createValidBookBuilder().build();
        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest(name = "Validation for {0} should fail")
    @MethodSource("provideInvalidBookArguments")
    void shouldFailValidationWhenFieldIsInvalid(String fieldName, Object invalidValue) {
        BookImpl.BookImplBuilder builder = CatalogTestUtils.createValidBookBuilder();

        switch (fieldName) {
            case "isbn":
                builder.isbn((String) invalidValue);
                break;
            case "title":
                builder.title((String) invalidValue);
                break;
            case "pageCount":
                builder.pageCount((Integer) invalidValue);
                break;
            case "publicationDate":
                builder.publicationDate((LocalDate) invalidValue);
                break;
            case "description":
                builder.description((String) invalidValue);
                break;
            case "publisher":
                builder.publisher((String) invalidValue);
                break;
            case "coverImageId":
                builder.coverImageId((String) invalidValue);
                break;
            case "originalLanguage":
                builder.originalLanguage((String) invalidValue);
                break;
            default:
                fail("Test case for field " + fieldName + " not implemented.");
        }

        Book book = builder.build();
        Set<ConstraintViolation<Book>> violations = validator.validate(book);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals(fieldName)));
    }

    private static Stream<Arguments> provideInvalidBookArguments() {
        return Stream.of(
            Arguments.of("isbn", ""),
            Arguments.of("title", ""),
            Arguments.of("title", ".".repeat(256)),
            Arguments.of("pageCount", -1),
            Arguments.of("publicationDate", LocalDate.now().plusDays(1)),
            Arguments.of("description", ".".repeat(501)),
            Arguments.of("publisher", ".".repeat(256)),
            Arguments.of("coverImageId", ".".repeat(256)),
            Arguments.of("originalLanguage", ".".repeat(51))
        );
    }
}