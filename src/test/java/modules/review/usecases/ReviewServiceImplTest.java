package modules.review.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import modules.review.core.domain.Review;
import modules.review.core.usecases.ReviewServiceImpl;
import modules.review.core.usecases.repositories.ReviewRepository;
import modules.review.utils.ReviewTestUtils;
import modules.catalog.core.domain.Book;
import modules.catalog.core.usecases.BookService;
import modules.catalog.utils.CatalogTestUtils;
import modules.user.core.domain.User;
import modules.user.core.usecases.UserService;
import modules.user.utils.UserTestUtils;


public class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookService bookService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateReviewSuccessful() {

        Review reviewToCreate = ReviewTestUtils.createValidReviewWithText("test");
        UUID bookId = reviewToCreate.getBook().getBookId();
        UUID userId = reviewToCreate.getUser().getKeycloakUserId();
        Book existingBook = CatalogTestUtils.createValidBookWithId(bookId);
        User existingUser = UserTestUtils.createValidUserWithId(userId);

        when(bookService.getBookById(bookId)).thenReturn(Optional.of(existingBook));
        when(userService.findUserProfileById(userId)).thenReturn(Optional.of(existingUser));
        when(reviewRepository.save(any(Review.class))).thenReturn(reviewToCreate);



        Review createdReview = reviewService.createReview(reviewToCreate);



        assertNotNull(createdReview, "createReview should return a Review object");
        assertEquals(reviewToCreate.getReviewId(), createdReview.getReviewId(),
                "Returned review should have the same ID as the input review");
        verify(reviewRepository, times(1)).save(reviewToCreate);
        verify(bookService, times(1)).getBookById(bookId);
        verify(userService, times(1)).findUserProfileById(userId);
    }

    @Test
    void testCreateReviewFailsBookNotFound() {
        Review reviewToCreate = ReviewTestUtils.createValidReviewWithText("test");
        UUID bookId = reviewToCreate.getBook().getBookId();

        when(bookService.getBookById(bookId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.createReview(reviewToCreate);
        }, "createReview should throw IllegalArgumentException if Book not found");

        verify(reviewRepository, never()).save(any(Review.class));
        verify(bookService, times(1)).getBookById(bookId);
    }


    @Test
    void testCreateReviewFailsUserNotFound() {
        Review reviewToCreate = ReviewTestUtils.createValidReviewWithText("test");
        UUID bookId = reviewToCreate.getBook().getBookId();
        UUID userId = reviewToCreate.getUser().getKeycloakUserId();

        when(bookService.getBookById(bookId))
                .thenReturn(Optional.of(CatalogTestUtils.createValidBookWithId(bookId)));
        when(userService.findUserProfileById(userId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.createReview(reviewToCreate);
        }, "createReview should throw IllegalArgumentException if User not found");

        verify(reviewRepository, never()).save(any(Review.class));
        verify(userService, times(1)).findUserProfileById(userId);
    }



    @Test
    void testfindReviewByIdSuccessful() {
        UUID reviewId = UUID.randomUUID();
        Review existingReview = ReviewTestUtils.createValidReviewWithIdAndText(reviewId, "test");
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(existingReview));

        Optional<Review> retrievedReviewOpt = reviewService.findReviewById(reviewId);

        assertTrue(retrievedReviewOpt.isPresent(),
                "findReviewById should return Optional.of(Review) if review exists");
        Review retrievedReview = retrievedReviewOpt.get();
        assertEquals(reviewId, retrievedReview.getReviewId(),
                "Retrieved review should have the correct ID");
        verify(reviewRepository, times(1)).findById(reviewId);
    }

    @Test
    void testfindReviewByIdFails() {
        UUID reviewId = UUID.randomUUID();
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        Optional<Review> retrievedReviewOpt = reviewService.findReviewById(reviewId);

        assertFalse(retrievedReviewOpt.isPresent(),
                "findReviewById should return Optional.empty() if review does not exist");
        verify(reviewRepository, times(1)).findById(reviewId);
    }

    @Test
    void testGetReviewsForBookSuccessful() {
        UUID bookId = UUID.randomUUID();
        Book existingBook = CatalogTestUtils.createValidBookWithId(bookId);
        List<Review> expectedReviews =
                Arrays.asList(ReviewTestUtils.createValidReviewForBook(bookId, "Review 1"),
                        ReviewTestUtils.createValidReviewForBook(bookId, "Review 2"));

        when(bookService.getBookById(bookId)).thenReturn(Optional.of(existingBook));
        when(reviewRepository.getBookReviews(bookId)).thenReturn(expectedReviews);

        List<Review> retrievedReviews = reviewService.getReviewsForBook(bookId);

        assertNotNull(retrievedReviews, "getReviewsForBook should return a list");
        assertFalse(retrievedReviews.isEmpty(),
                "List of reviews should not be empty when reviews exist for book");
        assertEquals(expectedReviews.size(), retrievedReviews.size(),
                "List should contain the expected number of reviews");
        verify(reviewRepository, times(1)).getBookReviews(bookId);
        verify(bookService, times(1)).getBookById(bookId);
    }

    @Test
    void testGetEmptyReviewsForBookSuccessful() {
        UUID bookId = UUID.randomUUID();
        Book existingBook = CatalogTestUtils.createValidBookWithId(bookId);

        when(bookService.getBookById(bookId)).thenReturn(Optional.of(existingBook));
        when(reviewRepository.getBookReviews(bookId)).thenReturn(new ArrayList<>());

        List<Review> retrievedReviews = reviewService.getReviewsForBook(bookId);

        assertNotNull(retrievedReviews,
                "getReviewsForBook should return a list even if no reviews exist");
        assertTrue(retrievedReviews.isEmpty(),
                "List of reviews should be empty when no reviews exist for book");
        verify(reviewRepository, times(1)).getBookReviews(bookId);
        verify(bookService, times(1)).getBookById(bookId);
    }

    @Test
    void testGetReviewsForBookFails() {
        UUID bookId = UUID.randomUUID();

        when(bookService.getBookById(bookId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.getReviewsForBook(bookId);
        }, "getReviewsForBook should throw IllegalArgumentException if Book not found");

        verify(reviewRepository, never()).getBookReviews(any());
        verify(bookService, times(1)).getBookById(bookId);
    }

    @Test
    void testGetReviewsForUserSuccessful() {
        UUID userId = UUID.randomUUID();
        User existingUser = UserTestUtils.createValidUserWithId(userId);
        List<Review> expectedReviews =
                Arrays.asList(ReviewTestUtils.createValidReviewForUser(userId, "Review 1"),
                        ReviewTestUtils.createValidReviewForUser(userId, "Review 2"));

        when(userService.findUserProfileById(userId)).thenReturn(Optional.of(existingUser));
        when(reviewRepository.getUserReviews(userId)).thenReturn(expectedReviews);

        List<Review> retrievedReviews = reviewService.getReviewsForUser(userId);

        assertNotNull(retrievedReviews, "getReviewsForUser should return a list");
        assertFalse(retrievedReviews.isEmpty(),
                "List of reviews should not be empty when reviews exist for user");
        assertEquals(expectedReviews.size(), retrievedReviews.size(),
                "List should contain the expected number of reviews");
        verify(reviewRepository, times(1)).getUserReviews(userId);
        verify(userService, times(1)).findUserProfileById(userId);
    }

    @Test
    void testGetEmptyReviewsForUserSuccessful() {
        UUID userId = UUID.randomUUID();
        User existingUser = UserTestUtils.createValidUserWithId(userId);

        when(userService.findUserProfileById(userId)).thenReturn(Optional.of(existingUser));
        when(reviewRepository.getUserReviews(userId)).thenReturn(new ArrayList<>());

        List<Review> retrievedReviews = reviewService.getReviewsForUser(userId);

        assertNotNull(retrievedReviews,
                "getReviewsForUser should return a list even if no reviews exist");
        assertTrue(retrievedReviews.isEmpty(),
                "List of reviews should be empty when no reviews exist for user");
        verify(reviewRepository, times(1)).getUserReviews(userId);
        verify(userService, times(1)).findUserProfileById(userId);
    }

    @Test
    void testGetReviewsForUserFails() {
        UUID userId = UUID.randomUUID();

        when(userService.findUserProfileById(userId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.getReviewsForUser(userId);
        }, "getReviewsForUser should throw IllegalArgumentException if User not found");

        verify(reviewRepository, never()).getUserReviews(any());
        verify(userService, times(1)).findUserProfileById(userId);
    }

    @Test
    void testUpdateReviewSuccessful() {
        Review reviewToUpdate = ReviewTestUtils.createValidReviewWithText("test");

        when(reviewRepository.findById(reviewToUpdate.getReviewId()))
                .thenReturn(Optional.of(reviewToUpdate));
        when(reviewRepository.save(any(Review.class))).thenReturn(reviewToUpdate);

        Review updatedReview = reviewService.updateReview(reviewToUpdate);

        assertNotNull(updatedReview, "updateReview should return a Review object");
        assertEquals(reviewToUpdate.getReviewId(), updatedReview.getReviewId(),
                "Returned review should have the same ID as the input review");
        verify(reviewRepository, times(1)).save(reviewToUpdate);
    }


    @Test
    void testUpdateReviewFails() {
        Review reviewToUpdate = ReviewTestUtils.createValidReviewWithText("test");

        when(reviewRepository.findById(reviewToUpdate.getReviewId())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.updateReview(reviewToUpdate);
        }, "updateReview should throw IllegalArgumentException if Review not found");
    }

    @Test
    void testDeleteReviewByIdSuccessful() {
        UUID reviewIdToDelete = UUID.randomUUID();
        Review reviewToDelete =
                ReviewTestUtils.createValidReviewWithIdAndText(reviewIdToDelete, "test");
        when(reviewRepository.findById(reviewIdToDelete)).thenReturn(Optional.of(reviewToDelete));
        doNothing().when(reviewRepository).deleteById(reviewIdToDelete);

        reviewService.deleteReviewById(reviewIdToDelete);

        verify(reviewRepository, times(1)).deleteById(reviewIdToDelete);
    }

    @Test
    void testGetAverageRatingForBookSuccessful() {
        UUID bookId = UUID.randomUUID();
        Book existingBook = CatalogTestUtils.createValidBookWithId(bookId);
        List<Review> reviewsForBook =
                Arrays.asList(ReviewTestUtils.createReviewWithRatingForBook(bookId, 4),
                        ReviewTestUtils.createReviewWithRatingForBook(bookId, 5),
                        ReviewTestUtils.createReviewWithRatingForBook(bookId, 3));
        when(bookService.getBookById(bookId)).thenReturn(Optional.of(existingBook));
        when(reviewRepository.getBookReviews(bookId)).thenReturn(reviewsForBook);

        double averageRating = reviewService.getAverageRatingForBook(bookId);

        assertEquals(4.0, averageRating, 0.001, "Average rating should be calculated correctly");
        verify(reviewRepository, times(1)).getBookReviews(bookId);
    }

    @Test
    void testGetAverageRatingForBookFails() {
        UUID bookId = UUID.randomUUID();
        when(bookService.getBookById(bookId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.getAverageRatingForBook(bookId);
        }, "getAverageRatingForBook should throw IllegalArgumentException if Book not found");

        verify(reviewRepository, never()).getBookReviews(any());
        verify(bookService, times(1)).getBookById(bookId);

    }


    @Test
    void testGetZeroAverageRatingForBookSuccessful() {
        UUID bookId = UUID.randomUUID();
        Book existingBook = CatalogTestUtils.createValidBookWithId(bookId);
        when(bookService.getBookById(bookId)).thenReturn(Optional.of(existingBook));
        when(reviewRepository.getBookReviews(bookId)).thenReturn(new ArrayList<>());

        double averageRating = reviewService.getAverageRatingForBook(bookId);

        assertEquals(0.0, averageRating, 0.001, "Average rating should be 0 when no reviews exist");
        verify(reviewRepository, times(1)).getBookReviews(bookId);
    }

    @Test
    void testFindReviewByUserAndBookSuccessful() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        Book existingBook = CatalogTestUtils.createValidBookWithId(bookId);
        User existingUser = UserTestUtils.createValidUserWithId(userId);
        Review existingReview = ReviewTestUtils.createValidReviewWithText("test");

        when(bookService.getBookById(bookId)).thenReturn(Optional.of(existingBook));
        when(userService.findUserProfileById(userId)).thenReturn(Optional.of(existingUser));
        when(reviewRepository.findByUserIdAndBookId(userId, bookId))
                .thenReturn(Optional.of(existingReview));

        Optional<Review> retrievedReviewOpt = reviewService.findReviewByUserAndBook(userId, bookId);

        assertTrue(retrievedReviewOpt.isPresent(),
                "findReviewByUserAndBook should return Optional.of(Review) if review exists");
        Review retrievedReview = retrievedReviewOpt.get();
        assertEquals(existingReview.getReviewId(), retrievedReview.getReviewId(),
                "Retrieved review should be the existing review");
        verify(reviewRepository, times(1)).findByUserIdAndBookId(userId, bookId);
        verify(bookService, times(1)).getBookById(bookId);
        verify(userService, times(1)).findUserProfileById(userId);
    }

    @Test
    void testFindReviewByUserAndBookFailsBookNotFound() {
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(bookService.getBookById(bookId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.findReviewByUserAndBook(userId, bookId);
        }, "findReviewByUserAndBook should throw IllegalArgumentException if Book not found");

        verify(reviewRepository, never()).findByUserIdAndBookId(any(), any());
        verify(bookService, times(1)).getBookById(bookId);
        verify(userService, never()).findUserProfileById(any());
    }

    @Test
    void testFindReviewByUserAndBookFailsUserNotFound() {
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Book existingBook = CatalogTestUtils.createValidBookWithId(bookId);

        when(bookService.getBookById(bookId)).thenReturn(Optional.of(existingBook));
        when(userService.findUserProfileById(userId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.findReviewByUserAndBook(userId, bookId);
        }, "findReviewByUserAndBook should throw IllegalArgumentException if User not found");

        verify(reviewRepository, never()).findByUserIdAndBookId(any(), any());
        verify(bookService, times(1)).getBookById(bookId);
        verify(userService, times(1)).findUserProfileById(userId);
    }

}
