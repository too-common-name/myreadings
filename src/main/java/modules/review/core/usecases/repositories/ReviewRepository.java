package modules.review.core.usecases.repositories;

import java.util.UUID;

import modules.review.core.domain.Review;

import java.util.Optional;
import java.util.List;

public interface ReviewRepository {
    Review create(Review review);
    Review update(Review review);
    List<Review> saveAll(Iterable<Review> reviews);
    Optional<Review> findById(UUID reviewId);
    Optional<Review> findByUserIdAndBookId(UUID userId, UUID bookId);
    List<Review> findAll();
    void deleteById(UUID reviewId);
    List<Review> getBookReviews(UUID bookId);
    List<Review> getUserReviews(UUID userId);
    Long countReviewsByBookId(UUID bookId);
    Double findAverageRatingByBookId(UUID bookId);
}