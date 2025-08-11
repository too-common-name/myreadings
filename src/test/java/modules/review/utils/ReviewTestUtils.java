package modules.review.utils;

import java.time.LocalDateTime;
import java.util.UUID;
import modules.catalog.utils.CatalogTestUtils;
import modules.review.core.domain.Review;
import modules.review.core.domain.ReviewImpl;

import modules.user.utils.UserTestUtils;

public class ReviewTestUtils {

    public static ReviewImpl createValidReviewWithIdAndText(UUID reviewId, String reviewText) {
        return ReviewImpl.builder().reviewId(reviewId)
                .book(CatalogTestUtils.createValidBook()).user(UserTestUtils.createValidUser())
                .reviewText(reviewText).rating(4).publicationDate(LocalDateTime.now()).build();
    }

    public static ReviewImpl createValidReviewWithText(String reviewText) {
        return ReviewImpl.builder().reviewId(UUID.randomUUID())
                .book(CatalogTestUtils.createValidBook()).user(UserTestUtils.createValidUser())
                .reviewText(reviewText).rating(4).publicationDate(LocalDateTime.now()).build();
    }

    public static ReviewImpl createValidReviewForUserAndBook(UUID userId, UUID bookId,
            String reviewText, int rating) {
        return ReviewImpl.builder().reviewId(UUID.randomUUID())
                .book(CatalogTestUtils.createValidBookWithId(bookId))
                .user(UserTestUtils.createValidUserWithId(userId)).reviewText(reviewText).rating(rating)
                .publicationDate(LocalDateTime.now()).build();
    }

    public static ReviewImpl.ReviewImplBuilder from(Review review) {
        return ReviewImpl.builder()
                .reviewId(review.getReviewId())
                .user(review.getUser())
                .book(review.getBook())
                .publicationDate(review.getPublicationDate())
                .rating(review.getRating())
                .reviewText(review.getReviewText());
    }
}
