package org.modular.playground.review.usecases;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modular.playground.catalog.core.domain.Book;
import org.modular.playground.catalog.core.domain.BookImpl;
import org.modular.playground.catalog.core.usecases.BookService;
import org.modular.playground.review.core.domain.Review;
import org.modular.playground.review.core.domain.ReviewImpl;
import org.modular.playground.review.core.usecases.ReviewServiceImpl;
import org.modular.playground.review.core.usecases.repositories.ReviewRepository;
import org.modular.playground.review.infrastructure.persistence.postgres.mapper.ReviewMapper;
import org.modular.playground.review.infrastructure.persistence.postgres.mapper.ReviewMapperImpl;
import org.modular.playground.review.web.dto.ReviewRequestDTO;
import org.modular.playground.user.core.domain.User;
import org.modular.playground.user.core.domain.UserImpl;
import org.modular.playground.user.core.usecases.UserService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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

    @Spy
    private ReviewMapper reviewMapper = new ReviewMapperImpl();

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

        assertThrows(NotFoundException.class, () -> reviewService.createReview(request, jwt));
        verify(reviewRepository, never()).create(any(Review.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingReviewForNonExistentUser() {
        ReviewRequestDTO request = ReviewRequestDTO.builder().bookId(testBook.getBookId()).build();
        when(jwt.getSubject()).thenReturn(testUser.getKeycloakUserId().toString());
        when(bookService.getBookById(testBook.getBookId())).thenReturn(Optional.of(testBook));
        when(userService.findUserProfileById(testUser.getKeycloakUserId(), jwt)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> reviewService.createReview(request, jwt));
        verify(reviewRepository, never()).create(any(Review.class));
    }

    @Test
    void shouldFindReviewByIdAndEnrichIt() {
        when(reviewRepository.findById(testReview.getReviewId())).thenReturn(Optional.of(testReview));
        when(userService.findUserByIdInternal(testUser.getKeycloakUserId())).thenReturn(Optional.of(testUser));
        when(bookService.getBookById(testBook.getBookId())).thenReturn(Optional.of(testBook));

        Optional<Review> result = reviewService.findReviewById(testReview.getReviewId(), jwt);

        assertTrue(result.isPresent());
        assertEquals(testReview.getReviewId(), result.get().getReviewId());
        verify(userService, times(1)).findUserByIdInternal(testUser.getKeycloakUserId());
        verify(bookService, times(1)).getBookById(testBook.getBookId());
    }

    @Test
    void shouldReturnEmptyOptionalWhenFindingNonExistentReviewById() {
        UUID nonExistentId = UUID.randomUUID();
        when(reviewRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        Optional<Review> result = reviewService.findReviewById(nonExistentId, jwt);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindReviewByUserAndBook() {
        when(reviewRepository.findByUserIdAndBookId(testUser.getKeycloakUserId(), testBook.getBookId())).thenReturn(Optional.of(testReview));
        when(userService.findUserByIdInternal(testUser.getKeycloakUserId())).thenReturn(Optional.of(testUser));
        when(bookService.getBookById(testBook.getBookId())).thenReturn(Optional.of(testBook));

        Optional<Review> result = reviewService.findReviewByUserAndBook(testUser.getKeycloakUserId(), testBook.getBookId(), jwt);

        assertTrue(result.isPresent());
        assertEquals(testReview.getReviewId(), result.get().getReviewId());
    }

    @Test
    void shouldReturnEnrichedReviewsForUser() {
        List<Review> rawReviews = List.of(testReview);
        when(userService.findUserProfileById(testUser.getKeycloakUserId(), jwt)).thenReturn(Optional.of(testUser));
        when(reviewRepository.getUserReviews(testUser.getKeycloakUserId())).thenReturn(rawReviews);
        when(userService.findUsersByIds(anyList())).thenReturn(List.of(testUser));
        when(bookService.getBooksByIds(anyList())).thenReturn(List.of(testBook));

        List<Review> result = reviewService.getReviewsForUser(testUser.getKeycloakUserId(), jwt);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(userService, times(1)).findUsersByIds(anyList());
        verify(bookService, times(1)).getBooksByIds(anyList());
    }

    @Test
    void shouldReturnEmptyListWhenGettingReviewsForNonExistentUser() {
        UUID userId = UUID.randomUUID();
        when(userService.findUserProfileById(any(), any())).thenReturn(Optional.empty());

        List<Review> reviews = reviewService.getReviewsForUser(userId, jwt);

        assertNotNull(reviews);
        assertTrue(reviews.isEmpty());
        verify(reviewRepository, never()).getUserReviews(any());
    }

    @Test
    void shouldHandleNullListFromRepositoryWhenEnriching() {
        when(userService.findUserProfileById(testUser.getKeycloakUserId(), jwt)).thenReturn(Optional.of(testUser));
        when(reviewRepository.getUserReviews(testUser.getKeycloakUserId())).thenReturn(null);

        List<Review> result = reviewService.getReviewsForUser(testUser.getKeycloakUserId(), jwt);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldGetReviewStatsForBook() {
        when(bookService.getBookById(testBook.getBookId())).thenReturn(Optional.of(testBook));
        when(reviewRepository.countReviewsByBookId(testBook.getBookId())).thenReturn(2L);
        when(reviewRepository.findAverageRatingByBookId(testBook.getBookId())).thenReturn(4.5);

        var stats = reviewService.getReviewStatsForBook(testBook.getBookId());

        assertNotNull(stats);
        assertEquals(2L, stats.getTotalReviews());
        assertEquals(4.5, stats.getAverageRating());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenGettingStatsForNonExistentBook() {
        UUID nonExistentBookId = UUID.randomUUID();
        when(bookService.getBookById(nonExistentBookId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> reviewService.getReviewStatsForBook(nonExistentBookId));
    }

    @Test
    void shouldUpdateReviewWhenUserIsOwner() {
        ReviewRequestDTO request = ReviewRequestDTO.builder().rating(5).reviewText("Updated!").build();

        when(jwt.getSubject()).thenReturn(testUser.getKeycloakUserId().toString());
        when(reviewRepository.findById(testReview.getReviewId())).thenReturn(Optional.of(testReview));
        when(reviewRepository.update(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Review updatedReview = reviewService.updateReview(testReview.getReviewId(), request, jwt);

        assertNotNull(updatedReview);
        assertEquals(5, updatedReview.getRating());
        assertEquals("Updated!", updatedReview.getReviewText());
        verify(reviewRepository, times(1)).update(any(Review.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdatingNonExistentReview() {
        UUID nonExistentReviewId = UUID.randomUUID();
        ReviewRequestDTO request = ReviewRequestDTO.builder().rating(5).reviewText("Updated!").build();
        when(reviewRepository.findById(nonExistentReviewId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> reviewService.updateReview(nonExistentReviewId, request, jwt));
    }

    @Test
    void shouldThrowForbiddenExceptionWhenUpdatingAnotherUsersReview() {
        ReviewRequestDTO request = ReviewRequestDTO.builder().rating(5).reviewText("Updated!").build();
        UUID otherUserId = UUID.randomUUID();

        when(jwt.getSubject()).thenReturn(otherUserId.toString());
        when(jwt.getClaim("realm_access")).thenReturn(null);
        when(reviewRepository.findById(testReview.getReviewId())).thenReturn(Optional.of(testReview));

        assertThrows(ForbiddenException.class, () -> reviewService.updateReview(testReview.getReviewId(), request, jwt));
        verify(reviewRepository, never()).update(any(Review.class));
    }

    @Test
    void shouldDeleteReviewWhenUserIsOwner() {
        when(jwt.getSubject()).thenReturn(testUser.getKeycloakUserId().toString());
        when(reviewRepository.findById(testReview.getReviewId())).thenReturn(Optional.of(testReview));
        doNothing().when(reviewRepository).deleteById(testReview.getReviewId());

        reviewService.deleteReviewById(testReview.getReviewId(), jwt);

        verify(reviewRepository, times(1)).deleteById(testReview.getReviewId());
    }

    @Test
    void shouldThrowForbiddenExceptionWhenDeletingAnotherUsersReview() {
        UUID otherUserId = UUID.randomUUID();
        when(jwt.getSubject()).thenReturn(otherUserId.toString());
        when(jwt.getClaim("realm_access")).thenReturn(null);
        when(reviewRepository.findById(testReview.getReviewId())).thenReturn(Optional.of(testReview));

        assertThrows(ForbiddenException.class, () -> reviewService.deleteReviewById(testReview.getReviewId(), jwt));
        verify(reviewRepository, never()).deleteById(any());
    }
}