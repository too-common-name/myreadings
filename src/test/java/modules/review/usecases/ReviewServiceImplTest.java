package modules.review.usecases;

import jakarta.ws.rs.ForbiddenException;
import modules.catalog.core.domain.Book;
import modules.catalog.core.domain.BookImpl;
import modules.catalog.core.usecases.BookService;
import modules.review.core.domain.Review;
import modules.review.core.domain.ReviewImpl;
import modules.review.core.usecases.ReviewServiceImpl;
import modules.review.core.usecases.repositories.ReviewRepository;
import modules.review.web.dto.ReviewRequestDTO;
import modules.user.core.domain.User;
import modules.user.core.domain.UserImpl;
import modules.user.core.usecases.UserService;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookService bookService;

    @Mock
    private UserService userService;

    @Mock
    private JsonWebToken jwt;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private User testUser;
    private Book testBook;
    private Review testReview;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        testUser = UserImpl.builder().keycloakUserId(userId).username("testuser").build();
        testBook = BookImpl.builder().bookId(bookId).title("Test Book").build();
        testReview = ReviewImpl.builder().reviewId(reviewId).user(testUser).book(testBook).rating(4).build();
    }

    @Test
    void shouldCreateReviewWhenRequestIsValid() {
        ReviewRequestDTO request = ReviewRequestDTO.builder()
                .bookId(testBook.getBookId()).rating(4).reviewText("Great!").build();

        when(jwt.getSubject()).thenReturn(testUser.getKeycloakUserId().toString());
        when(bookService.getBookById(testBook.getBookId())).thenReturn(Optional.of(testBook));
        when(userService.findUserProfileById(testUser.getKeycloakUserId(), jwt)).thenReturn(Optional.of(testUser));
        when(reviewRepository.create(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Review createdReview = reviewService.createReview(request, jwt);

        assertNotNull(createdReview);
        assertEquals(testBook.getBookId(), createdReview.getBook().getBookId());
        assertEquals(testUser.getKeycloakUserId(), createdReview.getUser().getKeycloakUserId());
        verify(reviewRepository, times(1)).create(any(Review.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingReviewForNonExistentBook() {
        ReviewRequestDTO request = ReviewRequestDTO.builder().bookId(testBook.getBookId()).build();
        when(jwt.getSubject()).thenReturn(testUser.getKeycloakUserId().toString());
        when(bookService.getBookById(testBook.getBookId())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.createReview(request, jwt);
        });
        verify(reviewRepository, never()).create(any(Review.class));
    }

    @Test
    void shouldUpdateReviewWhenUserIsOwner() {
        ReviewRequestDTO request = ReviewRequestDTO.builder().rating(5).reviewText("Updated!").build();

        when(jwt.getSubject()).thenReturn(testUser.getKeycloakUserId().toString());
        when(jwt.getClaim("realm_access")).thenReturn(null);
        when(reviewRepository.findById(testReview.getReviewId())).thenReturn(Optional.of(testReview));
        when(reviewRepository.update(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Review updatedReview = reviewService.updateReview(testReview.getReviewId(), request, jwt);

        assertNotNull(updatedReview);
        assertEquals(5, updatedReview.getRating());
        assertEquals("Updated!", updatedReview.getReviewText());
        verify(reviewRepository, times(1)).update(any(Review.class));
    }

    @Test
    void shouldThrowForbiddenExceptionWhenUpdatingAnotherUsersReview() {
        ReviewRequestDTO request = ReviewRequestDTO.builder().rating(5).reviewText("Updated!").build();
        UUID otherUserId = UUID.randomUUID();

        when(jwt.getSubject()).thenReturn(otherUserId.toString());
        when(jwt.getClaim("realm_access")).thenReturn(null);
        when(reviewRepository.findById(testReview.getReviewId())).thenReturn(Optional.of(testReview));

        assertThrows(ForbiddenException.class, () -> {
            reviewService.updateReview(testReview.getReviewId(), request, jwt);
        });
        verify(reviewRepository, never()).update(any(Review.class));
    }

    @Test
    void shouldDeleteReviewWhenUserIsOwner() {
        when(jwt.getSubject()).thenReturn(testUser.getKeycloakUserId().toString());
        when(jwt.getClaim("realm_access")).thenReturn(null);
        when(reviewRepository.findById(testReview.getReviewId())).thenReturn(Optional.of(testReview));
        doNothing().when(reviewRepository).deleteById(testReview.getReviewId());

        reviewService.deleteReviewById(testReview.getReviewId(), jwt);

        verify(reviewRepository, times(1)).deleteById(testReview.getReviewId());
    }

    @Test
    void shouldThrowExceptionWhenGettingReviewsForNonExistentUser() {
        UUID userId = UUID.randomUUID();
        when(userService.findUserProfileById(userId, jwt)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.getReviewsForUser(userId, jwt);
        });
        verify(reviewRepository, never()).getUserReviews(any());
    }
}