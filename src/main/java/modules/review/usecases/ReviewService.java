package modules.review.usecases;

import java.util.UUID;
import java.util.Optional;
import java.util.List;
import modules.review.domain.Review;

public interface ReviewService {

    Review createReview(Review review);

    Optional<Review> findReviewById(UUID reviewId);

    Optional<Review> findReviewByUserAndBook(UUID userId, UUID bookId);

    List<Review> getReviewsForBook(UUID bookId);

    List<Review> getReviewsForUser(UUID userId);

    Review updateReview(Review review); 

    void deleteReviewById(UUID reviewId);

    double getAverageRatingForBook(UUID bookId);
}