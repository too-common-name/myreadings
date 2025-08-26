package org.modular.playground.review.infrastructure;

import org.modular.playground.catalog.core.domain.Book;
import org.modular.playground.review.core.domain.Review;
import org.modular.playground.review.core.usecases.repositories.ReviewRepository;
import org.modular.playground.review.utils.ReviewTestUtils;
import org.modular.playground.user.core.domain.User;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractReviewRepositoryTest {

    protected abstract ReviewRepository getRepository();
    protected abstract User createAndSaveUser();
    protected abstract Book createAndSaveBook();

    protected void runTransactionalStep(Runnable step) {
        step.run();
    }

    protected <T> T runTransactionalStep(Supplier<T> step) {
        return step.get();
    }

    @Test
    void shouldCreateAndFindById() {
        User user = createAndSaveUser();
        Book book = createAndSaveBook();
        
        runTransactionalStep(() -> {
            Review review = ReviewTestUtils.createValidReviewForUserAndBook(user.getKeycloakUserId(), book.getBookId(), "Great book!", 5);
            Review createdReview = getRepository().create(review);
            Optional<Review> retrieved = getRepository().findById(createdReview.getReviewId());

            assertTrue(retrieved.isPresent());
            assertEquals(createdReview.getReviewId(), retrieved.get().getReviewId());
        });
    }

    @Test
    void shouldUpdateReview() {
        User user = createAndSaveUser();
        Book book = createAndSaveBook();
        
        Review originalReview = runTransactionalStep(() -> 
            getRepository().create(ReviewTestUtils.createValidReviewForUserAndBook(user.getKeycloakUserId(), book.getBookId(), "Original", 4))
        );
        
        runTransactionalStep(() -> {
            Review toUpdate = ReviewTestUtils.from(originalReview).reviewText("Updated Text").build();
            Review updatedReview = getRepository().update(toUpdate);
            assertEquals("Updated Text", updatedReview.getReviewText());
        });
    }

    @Test
    void shouldDeleteById() {
        User user = createAndSaveUser();
        Book book = createAndSaveBook();
        Review review = runTransactionalStep(() ->
            getRepository().create(ReviewTestUtils.createValidReviewForUserAndBook(user.getKeycloakUserId(), book.getBookId(), "To delete", 1))
        );

        runTransactionalStep(() -> {
            getRepository().deleteById(review.getReviewId());
            Optional<Review> result = getRepository().findById(review.getReviewId());
            assertTrue(result.isEmpty());
        });
    }

    @Test
    void shouldGetBookReviews() {
        User user = createAndSaveUser();
        Book book1 = createAndSaveBook();
        Book book2 = createAndSaveBook();

        runTransactionalStep(() -> {
            getRepository().create(ReviewTestUtils.createValidReviewForUserAndBook(user.getKeycloakUserId(), book1.getBookId(), "Review 1", 5));
            getRepository().create(ReviewTestUtils.createValidReviewForUserAndBook(user.getKeycloakUserId(), book1.getBookId(), "Review 2", 4));
            getRepository().create(ReviewTestUtils.createValidReviewForUserAndBook(user.getKeycloakUserId(), book2.getBookId(), "Review for other book", 3));
        });

        List<Review> reviews = runTransactionalStep(() -> getRepository().getBookReviews(book1.getBookId()));
        assertEquals(2, reviews.size());
    }

    @Test
    void shouldGetUserReviews() {
        User user1 = createAndSaveUser();
        User user2 = createAndSaveUser();
        Book book = createAndSaveBook();

        runTransactionalStep(() -> {
            getRepository().create(ReviewTestUtils.createValidReviewForUserAndBook(user1.getKeycloakUserId(), book.getBookId(), "Review 1", 5));
            getRepository().create(ReviewTestUtils.createValidReviewForUserAndBook(user1.getKeycloakUserId(), book.getBookId(), "Review 2", 4));
            getRepository().create(ReviewTestUtils.createValidReviewForUserAndBook(user2.getKeycloakUserId(), book.getBookId(), "Review from other user", 3));
        });

        List<Review> reviews = runTransactionalStep(() -> getRepository().getUserReviews(user1.getKeycloakUserId()));
        assertEquals(2, reviews.size());
    }
    
    @Test
    void shouldFindByUserIdAndBookId() {
        User user = createAndSaveUser();
        Book book = createAndSaveBook();
        
        runTransactionalStep(() -> {
            getRepository().create(ReviewTestUtils.createValidReviewForUserAndBook(user.getKeycloakUserId(), book.getBookId(), "Specific review", 5));
        });
        
        Optional<Review> foundReview = runTransactionalStep(() -> getRepository().findByUserIdAndBookId(user.getKeycloakUserId(), book.getBookId()));
        assertTrue(foundReview.isPresent());
    }

    @Test
    void shouldCountReviewsByBookId() {
        User user = createAndSaveUser();
        Book book = createAndSaveBook();
        
        runTransactionalStep(() -> {
            getRepository().create(ReviewTestUtils.createValidReviewForUserAndBook(user.getKeycloakUserId(), book.getBookId(), "Review 1", 5));
            getRepository().create(ReviewTestUtils.createValidReviewForUserAndBook(user.getKeycloakUserId(), book.getBookId(), "Review 2", 4));
        });

        long count = runTransactionalStep(() -> getRepository().countReviewsByBookId(book.getBookId()));
        assertEquals(2, count);
    }
    
    @Test
    void shouldFindAverageRatingByBookId() {
        User user = createAndSaveUser();
        Book book = createAndSaveBook();

        runTransactionalStep(() -> {
            getRepository().create(ReviewTestUtils.createValidReviewForUserAndBook(user.getKeycloakUserId(), book.getBookId(), "", 3));
            getRepository().create(ReviewTestUtils.createValidReviewForUserAndBook(user.getKeycloakUserId(), book.getBookId(), "", 5));
        });

        Double averageRating = runTransactionalStep(() -> getRepository().findAverageRatingByBookId(book.getBookId()));
        assertNotNull(averageRating);
        assertEquals(4.0, averageRating, 0.001);
    }

    @Test
    void shouldReturnNullForAverageRatingWhenNoReviewsExist() {
        Double averageRating = runTransactionalStep(() -> getRepository().findAverageRatingByBookId(UUID.randomUUID()));
        assertNull(averageRating);
    }
}