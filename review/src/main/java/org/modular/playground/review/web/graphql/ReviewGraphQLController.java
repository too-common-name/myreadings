package org.modular.playground.review.web.graphql;

import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import org.eclipse.microprofile.graphql.*;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;
import org.modular.playground.review.core.domain.Review;
import org.modular.playground.review.core.domain.ReviewStatsImpl;
import org.modular.playground.review.core.usecases.ReviewService;
import org.modular.playground.review.infrastructure.persistence.postgres.mapper.ReviewMapper;
import org.modular.playground.review.web.dto.ReviewRequestDTO;
import org.modular.playground.review.web.dto.ReviewResponseDTO;
import org.modular.playground.review.web.dto.ReviewStatsResponseDTO;

import java.util.List;
import java.util.UUID;

@GraphQLApi
@Authenticated
public class ReviewGraphQLController {

    private static final Logger LOGGER = Logger.getLogger(ReviewGraphQLController.class);

    @Inject
    ReviewService reviewService;

    @Inject
    JsonWebToken jwt;

    @Inject
    ReviewMapper reviewMapper;

    @Mutation
    @Description("Creates a new review for a book.")
    @RolesAllowed({"user", "admin"})
    public ReviewResponseDTO createReview(@Name("review") @Valid ReviewRequestDTO createReviewRequestDTO) {
        LOGGER.infof("GraphQL mutation to create review for book ID: %s", createReviewRequestDTO.getBookId());
        Review createdReview = reviewService.createReview(createReviewRequestDTO, jwt);
        return reviewMapper.toResponseDTO(createdReview);
    }

    @Query
    @Description("Finds a review by its unique ID.")
    @RolesAllowed({"user", "admin"})
    public ReviewResponseDTO reviewById(UUID reviewId) {
        LOGGER.infof("GraphQL query for review by ID: %s", reviewId);
        Review review = reviewService.findReviewAndCheckOwnership(reviewId, jwt);
        return reviewMapper.toResponseDTO(review);
    }

    @Mutation
    @Description("Updates an existing review.")
    @RolesAllowed({"user", "admin"})
    public ReviewResponseDTO updateReview(UUID reviewId, @Name("updates") @Valid ReviewRequestDTO updateReviewRequestDTO) {
        LOGGER.infof("GraphQL mutation to update review with ID: %s", reviewId);
        Review updatedReview = reviewService.updateReview(reviewId, updateReviewRequestDTO, jwt);
        return reviewMapper.toResponseDTO(updatedReview);
    }

    @Mutation
    @Description("Deletes a review by its unique ID.")
    @RolesAllowed({"user", "admin"})
    public boolean deleteReview(UUID reviewId) {
        LOGGER.infof("GraphQL mutation to delete review with ID: %s", reviewId);
        reviewService.deleteReviewById(reviewId, jwt);
        return true;
    }

    @Query
    @Description("Gets all reviews for a specific book.")
    @RolesAllowed({"user", "admin"})
    public List<ReviewResponseDTO> reviewsByBookId(UUID bookId) {
        LOGGER.infof("GraphQL query for reviews for book ID: %s", bookId);
        List<Review> reviews = reviewService.getReviewsForBook(bookId, jwt);
        return reviewMapper.toResponseDTOs(reviews);
    }

    @Query
    @Description("Gets the review statistics for a specific book.")
    @RolesAllowed({"user", "admin"})
    public ReviewStatsResponseDTO reviewStatsByBookId(UUID bookId) {
        LOGGER.infof("GraphQL query for review stats for book ID: %s", bookId);
        ReviewStatsImpl stats = reviewService.getReviewStatsForBook(bookId);
        return reviewMapper.toStatsResponseDTO(stats, bookId);
    }

    @Query
    @Description("Gets the current user's review for a specific book, if it exists.")
    @RolesAllowed({"user", "admin"})
    public ReviewResponseDTO myReviewForBook(UUID bookId) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());
        LOGGER.infof("GraphQL query for user %s's review for book ID: %s", currentUserId, bookId);
        return reviewService.findReviewByUserAndBook(currentUserId, bookId, jwt)
                .map(reviewMapper::toResponseDTO)
                .orElse(null);
    }

    @Query
    @Description("Gets all reviews written by a specific user.")
    @RolesAllowed({"user", "admin"})
    public List<ReviewResponseDTO> reviewsByUserId(UUID userId) {
        LOGGER.infof("GraphQL query for reviews for user ID: %s", userId);
        List<Review> reviews = reviewService.getReviewsForUser(userId, jwt);
        return reviewMapper.toResponseDTOs(reviews);
    }
}