package modules.review.infrastructure;

import modules.review.core.domain.Review;
import modules.review.infrastructure.persistence.in_memory.InMemoryReviewRepository;
import modules.review.utils.ReviewTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import common.InMemoryRepositoryTestProfile;
import io.quarkus.test.junit.TestProfile;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestProfile(InMemoryRepositoryTestProfile.class)
public class InMemoryReviewRepositoryTest {

    private InMemoryReviewRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryReviewRepository();
    }

    @Test
    void saveSuccessful() {
        Review reviewToSave = ReviewTestUtils.createValidReviewWithText("test");
        Review savedReview = repository.create(reviewToSave);
        Optional<Review> retrievedReview = repository.findById(savedReview.getReviewId());
        assertTrue(retrievedReview.isPresent());
        assertEquals(savedReview.getReviewId(), retrievedReview.get().getReviewId());
    }

    @Test
    void saveAllSuccessful() {
        List<Review> reviewsToSave = Arrays.asList(
            ReviewTestUtils.createValidReviewWithText("Review 1"),
            ReviewTestUtils.createValidReviewWithText("Review 2"),
            ReviewTestUtils.createValidReviewWithText("Review 3")
        );

        List<Review> savedReviews = repository.saveAll(reviewsToSave);

        assertNotNull(savedReviews, "saveAll should return a list of saved reviews");
        assertEquals(reviewsToSave.size(), savedReviews.size(), "saveAll should save all reviews in the input list");

        for (Review savedReview : savedReviews) {
            assertTrue(repository.findById(savedReview.getReviewId()).isPresent(), "Each review should be saved and exist in repository");
            Optional<Review> retrievedReviewOpt = repository.findById(savedReview.getReviewId());
            assertTrue(retrievedReviewOpt.isPresent());
            assertEquals(savedReview.getReviewId(), retrievedReviewOpt.get().getReviewId());
        }

        assertEquals(reviewsToSave.size(), repository.findAll().size(), "Repository count should match the number of saved reviews");
    }

    @Test
    void saveAllEmptyList() {
        List<Review> emptyReviewsList = new ArrayList<>();

        List<Review> savedReviews = repository.saveAll(emptyReviewsList);

        assertNotNull(savedReviews, "saveAll should return a list even for empty input");
        assertTrue(savedReviews.isEmpty(), "saveAll should return an empty list if input is empty");
        assertEquals(0, repository.findAll().size(), "Repository count should remain 0 if no reviews are saved");
    }

    @Test
    void saveAllDuplicateIdsShouldOverwrite() {
        UUID duplicateReviewId = UUID.randomUUID();
        Review review1 = ReviewTestUtils.createValidReviewWithIdAndText(duplicateReviewId, "Review with ID");
        Review review2WithDuplicateId = ReviewTestUtils.createValidReviewWithIdAndText(duplicateReviewId, "Review with DUPLICATE ID - overwrites");
        List<Review> reviewsToSave = Arrays.asList(review1, review2WithDuplicateId);

        List<Review> savedReviews = repository.saveAll(reviewsToSave);

        assertEquals(reviewsToSave.size(), savedReviews.size(), "saveAll should process all reviews in the input list");
        assertEquals(1, repository.findAll().size(), "Repository should contain only 1 review after saveAll with duplicate ID"); 
        
        Optional<Review> retrievedReviewOpt = repository.findById(duplicateReviewId);
        assertTrue(retrievedReviewOpt.isPresent(), "Review with duplicate ID should exist");
        assertEquals("Review with DUPLICATE ID - overwrites", retrievedReviewOpt.get().getReviewText(), "Last review with duplicate ID should overwrite previous one"); 
    }

    @Test
    void findByIdNotFound() {
        Optional<Review> retrievedReview = repository.findById(UUID.randomUUID());
        assertFalse(retrievedReview.isPresent());
    }

    @Test
    void findAllReviewsExist() {
        repository.create(ReviewTestUtils.createValidReviewWithText("test1"));
        repository.create(ReviewTestUtils.createValidReviewWithText("test2"));
        List<Review> allReviews = repository.findAll();
        assertEquals(2, allReviews.size());
    }

    @Test
    void findAllNoReviews() {
        List<Review> allReviews = repository.findAll();
        assertTrue(allReviews.isEmpty());
    }

    @Test
    void deleteByIdSuccessful() {
        Review reviewToDelete = ReviewTestUtils.createValidReviewWithText("test");
        Review savedReview = repository.create(reviewToDelete);
        repository.deleteById(savedReview.getReviewId());
        Optional<Review> deletedReview = repository.findById(savedReview.getReviewId());
        assertFalse(deletedReview.isPresent());
    }

    @Test
    void findReviewsByBookIdSuccessful() {
        UUID bookId1 = UUID.randomUUID();
        UUID bookId2 = UUID.randomUUID();
        Review reviewForBook1_1 = ReviewTestUtils.createValidReviewForBook(bookId1, "Review for book 1 - 1");
        Review reviewForBook1_2 = ReviewTestUtils.createValidReviewForBook(bookId1, "Review for book 1 - 2");
        Review reviewForBook2 = ReviewTestUtils.createValidReviewForBook(bookId2, "Review for book 2");
        repository.saveAll(Arrays.asList(reviewForBook1_1, reviewForBook1_2, reviewForBook2));

        List<Review> reviewsForBook1 = repository.getBookReviews(bookId1);
        assertEquals(2, reviewsForBook1.size());
        assertTrue(reviewsForBook1.stream().allMatch(review -> review.getBook().getBookId().equals(bookId1)));
    }

    @Test
    void findReviewsByBookIdFails() {
        UUID bookId = UUID.randomUUID();
        List<Review> reviewsForBook = repository.getBookReviews(bookId);
        assertTrue(reviewsForBook.isEmpty());
    }

    @Test
    void findReviewsByUserIdSuccessful() {
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        Review reviewForUser1_1 = ReviewTestUtils.createValidReviewForUser(userId1, "Review for user 1 - 1");
        Review reviewForUser1_2 = ReviewTestUtils.createValidReviewForUser(userId1, "Review for user 1 - 2");
        Review reviewForUser2 = ReviewTestUtils.createValidReviewForUser(userId2, "Review for user 2");
        repository.saveAll(Arrays.asList(reviewForUser1_1, reviewForUser1_2, reviewForUser2));

        List<Review> reviewsForUser1 = repository.getUserReviews(userId1);
        assertEquals(2, reviewsForUser1.size());
        assertTrue(reviewsForUser1.stream().allMatch(review -> review.getUser().getKeycloakUserId().equals(userId1)));
    }

    @Test
    void findReviewsByUserIdFails() {
        UUID userId = UUID.randomUUID();
        List<Review> reviewsForUser = repository.getUserReviews(userId);
        assertTrue(reviewsForUser.isEmpty());
    }

    @Test
    void findReviewsByUserIdAndUserIdSuccessful() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        Review expectedReview = ReviewTestUtils.createValidReviewForUserAndBook(userId, bookId, "Specific review by user for book");
        repository.create(expectedReview);

        Optional<Review> retrievedReviewOpt = repository.findByUserIdAndBookId(userId, bookId);
        assertTrue(retrievedReviewOpt.isPresent());
        assertEquals(expectedReview.getReviewId(), retrievedReviewOpt.get().getReviewId());
        assertEquals(userId, retrievedReviewOpt.get().getUser().getKeycloakUserId());
        assertEquals(bookId, retrievedReviewOpt.get().getBook().getBookId());
    }

    @Test
    void findReviewsByUserIdAndUserIdFails() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        Optional<Review> retrievedReviewOpt = repository.findByUserIdAndBookId(userId, bookId);
        assertFalse(retrievedReviewOpt.isPresent());
    }
}