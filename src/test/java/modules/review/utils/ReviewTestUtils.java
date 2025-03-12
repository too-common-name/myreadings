package modules.review.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;
import modules.catalog.domain.Book;
import modules.catalog.domain.BookImpl;
import modules.review.domain.Review;
import modules.review.domain.ReviewImpl;
import modules.user.domain.UiTheme;
import modules.user.domain.User;
import modules.user.domain.UserImpl;

public class ReviewTestUtils {

    public static Review createValidReviewWithIdAndText(UUID reviewId, String reviewText) {
        return new ReviewImpl.ReviewBuilder().reviewId(reviewId).book(createValidBook())
                .user(createValidUser()).reviewText(reviewText).rating(4)
                .publicationDate(LocalDateTime.now()).build();
    }

    public static Review createValidReviewWithText(String reviewText) {
        return new ReviewImpl.ReviewBuilder().reviewId(UUID.randomUUID()).book(createValidBook())
                .user(createValidUser()).reviewText(reviewText).rating(4)
                .publicationDate(LocalDateTime.now()).build();
    }

    public static Review createValidReviewForBook(UUID bookId, String reviewText) {
        return new ReviewImpl.ReviewBuilder().reviewId(UUID.randomUUID())
                .book(createValidBookWithId(bookId)).user(createValidUser()).reviewText(reviewText)
                .rating(4).publicationDate(LocalDateTime.now()).build();
    }

    public static Review createReviewWithRatingForBook(UUID bookId, int rating) {
        return new ReviewImpl.ReviewBuilder().reviewId(UUID.randomUUID())
                .book(createValidBookWithId(bookId)).user(createValidUser())
                .reviewText("Review with rating: " + rating).rating(rating)
                .publicationDate(LocalDateTime.now()).build();
    }

    public static Review createValidReviewForUser(UUID userId, String reviewText) {
        return new ReviewImpl.ReviewBuilder().reviewId(UUID.randomUUID()).book(createValidBook())
                .user(createValidUserWithId(userId)).reviewText(reviewText).rating(4)
                .publicationDate(LocalDateTime.now()).build();
    }


    public static Review createValidReviewForUserAndBook(UUID userId, UUID bookId,
            String reviewText) {
        return new ReviewImpl.ReviewBuilder().reviewId(UUID.randomUUID())
                .book(createValidBookWithId(bookId)).user(createValidUserWithId(userId))
                .reviewText(reviewText).rating(4).publicationDate(LocalDateTime.now()).build();
    }


    public static Book createValidBook() {
        return createValidBookWithId(UUID.randomUUID());
    }

    public static Book createValidBookWithId(UUID bookId) {
        return new BookImpl.BookBuilder().bookId(bookId).isbn("978-0321765723")
                .title("Test Book Title").authors(Arrays.asList("Test Author"))
                .publicationDate(LocalDate.now().minusYears(5)).publisher("Test Publisher")
                .description("Test book description").pageCount(300).coverImageId("coverTest123")
                .originalLanguage("en").build();
    }

    public static User createValidUser() {
        return createValidUserWithId(UUID.randomUUID());
    }

    public static User createValidUserWithId(UUID userId) {
        return new UserImpl.UserBuilder().userId(userId).firstName("Test").lastName("User")
                .username("testuser" + userId).email("test.user" + userId + "@example.com")
                .themePreference(UiTheme.LIGHT).build();
    }
}
