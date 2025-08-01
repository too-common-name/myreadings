package modules.review.infrastructure;

import modules.catalog.core.domain.Book;
import modules.catalog.utils.CatalogTestUtils;
import modules.review.core.domain.Review;
import modules.review.infrastructure.persistence.in_memory.InMemoryReviewRepository;
import modules.review.utils.ReviewTestUtils;
import modules.user.core.domain.User;
import modules.user.utils.UserTestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import common.InMemoryRepositoryTestProfile;
import io.quarkus.test.junit.TestProfile;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestProfile(InMemoryRepositoryTestProfile.class)
public class InMemoryReviewRepositoryTest {

    private InMemoryReviewRepository reviewRepository;

    private User testUser1;
    private Book testBook1;

    @BeforeEach
    void setUp() {
        reviewRepository = new InMemoryReviewRepository();
        
        testUser1 = UserTestUtils.createValidUser();
        testBook1 = CatalogTestUtils.createValidBook();
    }

     @Test
    void testCreateAndFindById() {
        Review reviewToCreate = ReviewTestUtils.createValidReviewForUserAndBook(testUser1.getKeycloakUserId(),
                testBook1.getBookId(), "Great book!", 5);

        Review createdReview = reviewRepository.create(reviewToCreate);
        assertNotNull(createdReview.getReviewId());

        Optional<Review> retrieved = reviewRepository.findById(createdReview.getReviewId());

        assertTrue(retrieved.isPresent());
        assertEquals(createdReview.getReviewId(), retrieved.get().getReviewId());
        assertEquals("Great book!", retrieved.get().getReviewText());
    }

    @Test
    void testUpdateReview() {
        Review createdReview = reviewRepository.create(
                ReviewTestUtils.createValidReviewForUserAndBook(testUser1.getKeycloakUserId(), testBook1.getBookId(),
                        "Original Text", 5));

        Review reviewToUpdate = Review.builder()
                .reviewId(createdReview.getReviewId())
                .user(createdReview.getUser())
                .book(createdReview.getBook())
                .publicationDate(createdReview.getPublicationDate())
                .rating(5)
                .reviewText("Updated Text!")
                .build();

        Review updatedReview = reviewRepository.update(reviewToUpdate);

        assertEquals(createdReview.getReviewId(), updatedReview.getReviewId());
        assertEquals(5, updatedReview.getRating());
        assertEquals("Updated Text!", updatedReview.getReviewText());

        Optional<Review> retrieved = reviewRepository.findById(createdReview.getReviewId());
        assertTrue(retrieved.isPresent());
        assertEquals(5, retrieved.get().getRating());
        assertEquals("Updated Text!", retrieved.get().getReviewText());
    }

    @Test
    void testDeleteById() {
        Review createdReview = reviewRepository.create(ReviewTestUtils.createValidReviewForUserAndBook(
                testUser1.getKeycloakUserId(), testBook1.getBookId(), "To be deleted", 5));
        UUID reviewId = createdReview.getReviewId();

        reviewRepository.deleteById(reviewId);

        Optional<Review> result = reviewRepository.findById(reviewId);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetBookReviews() {
        Book anotherBook = CatalogTestUtils.createValidBook();
        reviewRepository.create(ReviewTestUtils.createValidReviewForUserAndBook(testUser1.getKeycloakUserId(),
                testBook1.getBookId(), "Review 1 for book 1", 5));
        reviewRepository.create(ReviewTestUtils.createValidReviewForUserAndBook(testUser1.getKeycloakUserId(),
                testBook1.getBookId(), "Review 2 for book 1", 5));
        reviewRepository.create(ReviewTestUtils.createValidReviewForUserAndBook(testUser1.getKeycloakUserId(),
                anotherBook.getBookId(), "Review for another book", 5));

        List<Review> reviews = reviewRepository.getBookReviews(testBook1.getBookId());

        assertEquals(2, reviews.size());
        assertTrue(reviews.stream().allMatch(r -> r.getBook().getBookId().equals(testBook1.getBookId())));
    }

    @Test
    void testGetUserReviews() {
        User anotherUser = UserTestUtils.createValidUser();
        reviewRepository.create(ReviewTestUtils.createValidReviewForUserAndBook(testUser1.getKeycloakUserId(),
                testBook1.getBookId(), "Review 1 by user 1", 5));
        reviewRepository.create(ReviewTestUtils.createValidReviewForUserAndBook(testUser1.getKeycloakUserId(),
                testBook1.getBookId(), "Review 2 by user 1", 5));
        reviewRepository.create(ReviewTestUtils.createValidReviewForUserAndBook(anotherUser.getKeycloakUserId(),
                testBook1.getBookId(), "Review by another user", 5));

        List<Review> reviews = reviewRepository.getUserReviews(testUser1.getKeycloakUserId());

        assertEquals(2, reviews.size());
        assertTrue(
                reviews.stream().allMatch(r -> r.getUser().getKeycloakUserId().equals(testUser1.getKeycloakUserId())));
    }

    @Test
    void testFindByUserIdAndBookId() {
        reviewRepository.create(ReviewTestUtils.createValidReviewForUserAndBook(testUser1.getKeycloakUserId(),
                testBook1.getBookId(), "Specific review", 5));

        Optional<Review> foundReview = reviewRepository.findByUserIdAndBookId(testUser1.getKeycloakUserId(),
                testBook1.getBookId());

        assertTrue(foundReview.isPresent());
        assertEquals("Specific review", foundReview.get().getReviewText());

        Optional<Review> notFoundReview = reviewRepository.findByUserIdAndBookId(testUser1.getKeycloakUserId(),
                UUID.randomUUID());
        assertTrue(notFoundReview.isEmpty());
    }

    @Test
    void testCountReviewsByBookId() {
        reviewRepository.create(ReviewTestUtils.createValidReviewForUserAndBook(testUser1.getKeycloakUserId(), testBook1.getBookId(), "Review 1", 5));
        reviewRepository.create(ReviewTestUtils.createValidReviewForUserAndBook(testUser1.getKeycloakUserId(), testBook1.getBookId(), "Review 2", 5));
        reviewRepository.create(ReviewTestUtils.createValidReviewForUserAndBook(testUser1.getKeycloakUserId(), testBook1.getBookId(), "Review for other book", 5));

        long count = reviewRepository.countReviewsByBookId(testBook1.getBookId());
        assertEquals(3, count);

        long countNonExistent = reviewRepository.countReviewsByBookId(UUID.randomUUID());
        assertEquals(0, countNonExistent);
    }

    @Test
    void testFindAverageRatingByBookId_WithReviews() {
        reviewRepository.create(ReviewTestUtils.createValidReviewForUserAndBook(testUser1.getKeycloakUserId(), testBook1.getBookId(), "", 4));
        reviewRepository.create(ReviewTestUtils.createValidReviewForUserAndBook(testUser1.getKeycloakUserId(), testBook1.getBookId(), "", 5));
        reviewRepository.create(ReviewTestUtils.createValidReviewForUserAndBook(testUser1.getKeycloakUserId(), testBook1.getBookId(), "", 3));

        Double averageRating1 = reviewRepository.findAverageRatingByBookId(testBook1.getBookId());
        assertNotNull(averageRating1);
        assertEquals(4.0, averageRating1, 0.001);
    }

    @Test
    void testFindAverageRatingByBookId_NoReviews() {
        Double averageRating = reviewRepository.findAverageRatingByBookId(testBook1.getBookId());
        assertNull(averageRating);

        Double averageRatingNonExistentBook = reviewRepository.findAverageRatingByBookId(UUID.randomUUID());
        assertNull(averageRatingNonExistentBook);
    }
}