package modules.review.core.usecases;

import java.util.UUID;

import modules.review.core.domain.Review;
import modules.review.core.domain.ReviewStats;

import java.util.Optional;
import java.util.List;

public interface ReviewService {

    Review createReview(Review review);

    Optional<Review> findReviewById(UUID reviewId);

    Optional<Review> findReviewByUserAndBook(UUID userId, UUID bookId);

    List<Review> getReviewsForBook(UUID bookId);

    List<Review> getReviewsForUser(UUID userId);

    Review updateReview(Review review); 

    void deleteReviewById(UUID reviewId);

    ReviewStats getReviewStatsForBook(UUID bookId);
}