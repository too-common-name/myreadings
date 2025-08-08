package modules.review.core.usecases;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.eclipse.microprofile.jwt.JsonWebToken;
import modules.review.core.domain.Review;
import modules.review.core.domain.ReviewStats;
import modules.review.web.dto.ReviewRequestDTO;

public interface ReviewService {
    Review createReview(ReviewRequestDTO reviewRequest, JsonWebToken principal);
    Optional<Review> findReviewById(UUID reviewId);
    Review findReviewAndCheckOwnership(UUID reviewId, JsonWebToken principal);
    Optional<Review> findReviewByUserAndBook(UUID userId, UUID bookId, JsonWebToken principal);
    List<Review> getReviewsForBook(UUID bookId);
    List<Review> getReviewsForUser(UUID userId, JsonWebToken principal);
    Review updateReview(UUID reviewId, ReviewRequestDTO reviewRequest, JsonWebToken principal); 
    void deleteReviewById(UUID reviewId, JsonWebToken principal);
    ReviewStats getReviewStatsForBook(UUID bookId);
}