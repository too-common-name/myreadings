package modules.review.utils;

import java.time.LocalDateTime;
import java.util.UUID;
import modules.catalog.utils.CatalogTestUtils;
import modules.review.domain.Review;
import modules.review.domain.ReviewImpl;
import modules.user.utils.UserTestUtils;

public class ReviewTestUtils {

    public static Review createValidReviewWithIdAndText(UUID reviewId, String reviewText) {
        return new ReviewImpl.ReviewBuilder().reviewId(reviewId)
                .book(CatalogTestUtils.createValidBook()).user(UserTestUtils.createValidUser())
                .reviewText(reviewText).rating(4).publicationDate(LocalDateTime.now()).build();
    }

    public static Review createValidReviewWithText(String reviewText) {
        return new ReviewImpl.ReviewBuilder().reviewId(UUID.randomUUID())
                .book(CatalogTestUtils.createValidBook()).user(UserTestUtils.createValidUser())
                .reviewText(reviewText).rating(4).publicationDate(LocalDateTime.now()).build();
    }

    public static Review createValidReviewForBook(UUID bookId, String reviewText) {
        return new ReviewImpl.ReviewBuilder().reviewId(UUID.randomUUID())
                .book(CatalogTestUtils.createValidBookWithId(bookId))
                .user(UserTestUtils.createValidUser()).reviewText(reviewText).rating(4)
                .publicationDate(LocalDateTime.now()).build();
    }

    public static Review createReviewWithRatingForBook(UUID bookId, int rating) {
        return new ReviewImpl.ReviewBuilder().reviewId(UUID.randomUUID())
                .book(CatalogTestUtils.createValidBookWithId(bookId))
                .user(UserTestUtils.createValidUser()).reviewText("Review with rating: " + rating)
                .rating(rating).publicationDate(LocalDateTime.now()).build();
    }

    public static Review createValidReviewForUser(UUID userId, String reviewText) {
        return new ReviewImpl.ReviewBuilder().reviewId(UUID.randomUUID())
                .book(CatalogTestUtils.createValidBook())
                .user(UserTestUtils.createValidUserWithId(userId)).reviewText(reviewText).rating(4)
                .publicationDate(LocalDateTime.now()).build();
    }


    public static Review createValidReviewForUserAndBook(UUID userId, UUID bookId,
            String reviewText) {
        return new ReviewImpl.ReviewBuilder().reviewId(UUID.randomUUID())
                .book(CatalogTestUtils.createValidBookWithId(bookId))
                .user(UserTestUtils.createValidUserWithId(userId)).reviewText(reviewText).rating(4)
                .publicationDate(LocalDateTime.now()).build();
    }
}
