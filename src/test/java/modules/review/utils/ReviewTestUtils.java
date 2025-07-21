package modules.review.utils;

import java.time.LocalDateTime;
import java.util.UUID;
import modules.catalog.utils.CatalogTestUtils;
import modules.review.core.domain.Review;

import modules.user.utils.UserTestUtils;

public class ReviewTestUtils {

    public static Review createValidReviewWithIdAndText(UUID reviewId, String reviewText) {
        return Review.builder().reviewId(reviewId)
                .book(CatalogTestUtils.createValidBook()).user(UserTestUtils.createValidUser())
                .reviewText(reviewText).rating(4).publicationDate(LocalDateTime.now()).build();
    }

    public static Review createValidReviewWithText(String reviewText) {
        return Review.builder().reviewId(UUID.randomUUID())
                .book(CatalogTestUtils.createValidBook()).user(UserTestUtils.createValidUser())
                .reviewText(reviewText).rating(4).publicationDate(LocalDateTime.now()).build();
    }

    public static Review createValidReviewForUserAndBook(UUID userId, UUID bookId,
            String reviewText, int rating) {
        return Review.builder().reviewId(UUID.randomUUID())
                .book(CatalogTestUtils.createValidBookWithId(bookId))
                .user(UserTestUtils.createValidUserWithId(userId)).reviewText(reviewText).rating(rating)
                .publicationDate(LocalDateTime.now()).build();
    }
}
