package modules.review.infrastructure.persistence.in_memory;

import jakarta.enterprise.context.ApplicationScoped;
import modules.review.core.domain.Review;
import modules.review.core.usecases.repositories.ReviewRepository;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped 
public class InMemoryReviewRepository implements ReviewRepository {

    private final Map<UUID, Review> reviews = new HashMap<>();

    @Override
    public Review save(Review review) {
        reviews.put(review.getReviewId(), review);
        return review;
    }

    @Override
    public List<Review> saveAll(Iterable<Review> reviewsToSave) {
        List<Review> savedReviews = new ArrayList<>();
        for (Review review : reviewsToSave) {
            savedReviews.add(save(review));
        }
        return savedReviews;
    }

    @Override
    public Optional<Review> findById(UUID reviewId) {
        return Optional.ofNullable(reviews.get(reviewId));
    }

    @Override
    public List<Review> findAll() {
        return new ArrayList<>(reviews.values());
    }

    @Override
    public void deleteById(UUID reviewId) {
        reviews.remove(reviewId);
    }

    @Override
    public List<Review> getBookReviews(UUID bookId) {
        return reviews.values().stream()
                .filter(review -> review.getBook().getBookId().equals(bookId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Review> getUserReviews(UUID userId) {
        return reviews.values().stream()
                .filter(review -> review.getUser().getKeycloakUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Review> findByUserIdAndBookId(UUID userId, UUID bookId) {
        return reviews.values().stream()
                .filter(review -> review.getUser().getKeycloakUserId().equals(userId) && review.getBook().getBookId().equals(bookId))
                .findFirst();
    }

}