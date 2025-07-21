package modules.review.infrastructure;

import common.JpaRepositoryTestProfile;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import modules.catalog.core.domain.Book;
import modules.catalog.core.usecases.repositories.BookRepository;
import modules.catalog.utils.CatalogTestUtils;
import modules.review.core.domain.Review;

import modules.review.core.usecases.repositories.ReviewRepository;
import modules.review.utils.ReviewTestUtils;
import modules.user.core.domain.User;
import modules.user.core.usecases.repositories.UserRepository;
import modules.user.utils.UserTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestProfile(JpaRepositoryTestProfile.class)
@TestTransaction
public class JpaReviewRepositoryTest {

    @Inject
    ReviewRepository reviewRepository;
    @Inject
    UserRepository userRepository;
    @Inject
    BookRepository bookRepository;

    private User testUser1;
    private Book testBook1;

    @BeforeEach
    void setUp() {
        testUser1 = userRepository.save(UserTestUtils.createValidUser());
        testBook1 = bookRepository.save(CatalogTestUtils.createValidBook());
    }

    @Test
    void testCreateAndFindById() {
        Review reviewToCreate = ReviewTestUtils.createValidReviewForUserAndBook(testUser1.getKeycloakUserId(),
                testBook1.getBookId(), "Great book!", 5);

        Review createdReview = reviewRepository.create(reviewToCreate);
        assertNotNull(createdReview.getReviewId(), "Created review should have a non-null ID");

        Optional<Review> retrieved = reviewRepository.findById(createdReview.getReviewId());

        assertTrue(retrieved.isPresent(), "Review should be found by its new ID");
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
    }

    @Test
    void testDeleteById() {
        Review createdReview = reviewRepository.create(ReviewTestUtils.createValidReviewForUserAndBook(
                testUser1.getKeycloakUserId(), testBook1.getBookId(), "To be deleted", 5));
        UUID reviewId = createdReview.getReviewId();

        reviewRepository.deleteById(reviewId);

        Optional<Review> result = reviewRepository.findById(reviewId);
        assertTrue(result.isEmpty(), "Review should be deleted");
    }

    @Test
    void testGetBookReviews() {
        Book anotherBook = bookRepository.save(CatalogTestUtils.createValidBook());
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
        User anotherUser = userRepository.save(UserTestUtils.createValidUser());
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

    @Test
    void testFindAverageRatingByBookId_NonExistentBookId() {
        Double averageRatingNonExistentBook = reviewRepository.findAverageRatingByBookId(UUID.randomUUID());
        assertNull(averageRatingNonExistentBook);
    }
}