package org.modular.playground.review.infrastructure.persistence.in_memory;

import jakarta.enterprise.context.ApplicationScoped;
import org.modular.playground.review.core.domain.Review;
import org.modular.playground.review.core.usecases.repositories.ReviewRepository;
import io.quarkus.arc.properties.IfBuildProperty;

import org.jboss.logging.Logger;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
@IfBuildProperty(name = "app.repository.type", stringValue = "in-memory", enableIfMissing = true)
public class InMemoryReviewRepository implements ReviewRepository {

    private static final Logger LOGGER = Logger.getLogger(InMemoryReviewRepository.class);
    private final Map<UUID, Review> reviews = new HashMap<>();

    @Override
    public Review create(Review review) {
        LOGGER.debugf("In-memory: Creating review with ID: %s", review.getReviewId());
        reviews.put(review.getReviewId(), review);
        return review;
    }

    @Override
    public Review update(Review review) {
        LOGGER.debugf("In-memory: Updating review with ID: %s", review.getReviewId());
        reviews.put(review.getReviewId(), review);
        return review;
    }

    @Override
    public Optional<Review> findById(UUID reviewId) {
        LOGGER.debugf("In-memory: Finding review by ID: %s", reviewId);
        return Optional.ofNullable(reviews.get(reviewId));
    }

    @Override
    public void deleteById(UUID reviewId) {
        LOGGER.debugf("In-memory: Deleting review with ID: %s", reviewId);
        reviews.remove(reviewId);
    }

    @Override
    public List<Review> getBookReviews(UUID bookId) {
        LOGGER.debugf("In-memory: Getting reviews for book ID: %s", bookId);
        return reviews.values().stream()
                .filter(review -> review.getBook().getBookId().equals(bookId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Review> getUserReviews(UUID userId) {
        LOGGER.debugf("In-memory: Getting reviews for user ID: %s", userId);
        return reviews.values().stream()
                .filter(review -> review.getUser().getKeycloakUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Review> findByUserIdAndBookId(UUID userId, UUID bookId) {
        LOGGER.debugf("In-memory: Finding review by user ID %s and book ID %s", userId, bookId);
        return reviews.values().stream()
                .filter(review -> review.getUser().getKeycloakUserId().equals(userId)
                        && review.getBook().getBookId().equals(bookId))
                .findFirst();
    }

    @Override
    public Long countReviewsByBookId(UUID bookId) {
        LOGGER.debugf("In-memory: Counting reviews for book ID: %s", bookId);
        return reviews.values().stream()
                .filter(review -> review.getBook().getBookId().equals(bookId))
                .count();
    }

    @Override
    public Double findAverageRatingByBookId(UUID bookId) {
        LOGGER.debugf("In-memory: Finding average rating for book ID: %s", bookId);
        OptionalDouble average = reviews.values().stream()
                .filter(review -> review.getBook().getBookId().equals(bookId))
                .mapToDouble(Review::getRating)
                .average();
        return average.isPresent() ? average.getAsDouble() : null;
    }

    @Override
    public void deleteAll() {
        LOGGER.debug("In-memory: Deleting all reviews");
        reviews.clear();
    }
}