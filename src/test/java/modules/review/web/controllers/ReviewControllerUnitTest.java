package modules.review.web.controllers;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import modules.catalog.core.domain.Book;
import modules.catalog.core.domain.BookImpl;
import modules.catalog.core.usecases.BookService;
import modules.review.core.domain.Review;
import modules.review.core.domain.ReviewStats;
import modules.review.core.usecases.ReviewService;
import modules.review.web.dto.ReviewRequestDTO;
import modules.review.web.dto.ReviewResponseDTO;
import modules.review.web.dto.ReviewStatsResponseDTO;
import modules.user.core.domain.User;
import modules.user.core.domain.UserImpl;
import modules.user.core.usecases.UserService;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewControllerUnitTest {

    @InjectMocks
    private ReviewController reviewController;

    @Mock
    private ReviewService reviewService;

    @Mock
    private BookService bookService;

    @Mock
    private UserService userService;

    @Mock
    private JsonWebToken jwt;

    private UUID testUserId;
    private UUID testBookId;
    private UUID testReviewId;

    private Review mockReview;
    private ReviewRequestDTO mockReviewRequestDTO;
    private ReviewResponseDTO expectedResponseDTO;
    private Book mockBook;
    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUserId = UUID.randomUUID();
        testBookId = UUID.randomUUID();
        testReviewId = UUID.randomUUID();

        mockBook = BookImpl.builder().bookId(testBookId).title("Test Book").build();
        mockUser = UserImpl.builder().keycloakUserId(testUserId).username("testuser").build();

        mockReviewRequestDTO = ReviewRequestDTO.builder()
                .bookId(testBookId)
                .rating(4)
                .reviewText("This is a test review.")
                .build();

        mockReview = Review.builder()
                .reviewId(testReviewId)
                .book(mockBook)
                .user(mockUser)
                .rating(4)
                .reviewText("This is a test review.")
                .publicationDate(LocalDateTime.now())
                .build();

        expectedResponseDTO = ReviewResponseDTO.builder()
                .reviewId(testReviewId)
                .bookId(testBookId)
                .userId(testUserId)
                .rating(4)
                .reviewText("This is a test review.")
                .publicationDate(mockReview.getPublicationDate())
                .username("testuser")
                .build();
    }

    @Test
    void testCreateReviewShouldReturnCreatedAndReviewDTO() {
        when(bookService.getBookById(testBookId)).thenReturn(Optional.of(mockBook));
        when(userService.findUserProfileById(testUserId)).thenReturn(Optional.of(mockUser));
        when(reviewService.createReview(any(Review.class))).thenReturn(mockReview);
        when(jwt.getClaim("sub")).thenReturn(testUserId.toString());

        Response response = reviewController.createReview(mockReviewRequestDTO);

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertEquals(expectedResponseDTO, response.getEntity());
        verify(reviewService, times(1)).createReview(any(Review.class));
    }

    @Test
    void testCreateReviewShouldReturnBadRequestIfBookNotFound() {
        when(bookService.getBookById(testBookId)).thenReturn(Optional.empty());
        when(jwt.getClaim("sub")).thenReturn(testUserId.toString());

        Response response = reviewController.createReview(mockReviewRequestDTO);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Book not found.", response.getEntity());
        verify(reviewService, never()).createReview(any());
    }

    @Test
    void testCreateReviewShouldReturnBadRequestIfUserNotFound() {
        when(bookService.getBookById(testBookId)).thenReturn(Optional.of(mockBook));
        when(userService.findUserProfileById(testUserId)).thenReturn(Optional.empty());
        when(jwt.getClaim("sub")).thenReturn(testUserId.toString());

        Response response = reviewController.createReview(mockReviewRequestDTO);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("User not found.", response.getEntity());
        verify(reviewService, never()).createReview(any());
    }

    @Test
    void testGetReviewByIdShouldReturnOkAndReviewDTO() {
        when(reviewService.findReviewById(testReviewId)).thenReturn(Optional.of(mockReview));
        when(jwt.getClaim("sub")).thenReturn(testUserId.toString());

        Response response = reviewController.getReviewById(testReviewId);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(expectedResponseDTO, response.getEntity());
        verify(reviewService, times(1)).findReviewById(testReviewId);
    }

    @Test
    void testGetReviewByIdShouldReturnNotFound() {
        when(reviewService.findReviewById(testReviewId)).thenReturn(Optional.empty());
        when(jwt.getClaim("sub")).thenReturn(testUserId.toString());

        Response response = reviewController.getReviewById(testReviewId);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertNull(response.getEntity());
        verify(reviewService, times(1)).findReviewById(testReviewId);
    }

    @Test
    void testUpdateReviewShouldReturnOkAndReviewDTOForOwner() {
        ReviewRequestDTO updateRequestDTO = ReviewRequestDTO.builder().rating(5).reviewText("Updated review.").bookId(testBookId).build();
        Review updatedReview = Review.builder()
                .reviewId(testReviewId)
                .book(mockBook)
                .user(mockUser)
                .rating(5)
                .reviewText("Updated review.")
                .publicationDate(LocalDateTime.now())
                .build();
        ReviewResponseDTO updatedResponseDTO = ReviewResponseDTO.builder()
                .reviewId(testReviewId)
                .bookId(testBookId)
                .userId(testUserId)
                .rating(5)
                .reviewText("Updated review.")
                .publicationDate(updatedReview.getPublicationDate())
                .username("testuser")
                .build();

        when(reviewService.findReviewById(testReviewId)).thenReturn(Optional.of(mockReview));
        when(bookService.getBookById(testBookId)).thenReturn(Optional.of(mockBook));
        when(userService.findUserProfileById(testUserId)).thenReturn(Optional.of(mockUser));
        when(reviewService.updateReview(any(Review.class))).thenReturn(updatedReview);
        when(jwt.getClaim("sub")).thenReturn(testUserId.toString());

        Response response = reviewController.updateReview(testReviewId, updateRequestDTO);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(updatedResponseDTO, response.getEntity());
        verify(reviewService, times(1)).updateReview(any(Review.class));
    }

    @Test
    void testUpdateReviewShouldReturnNotFoundIfReviewDoesNotExist() {
        when(reviewService.findReviewById(testReviewId)).thenReturn(Optional.empty());
        when(jwt.getClaim("sub")).thenReturn(testUserId.toString());

        Response response = reviewController.updateReview(testReviewId, mockReviewRequestDTO);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertNull(response.getEntity());
        verify(reviewService, never()).updateReview(any());
    }

    @Test
    void testUpdateReviewShouldReturnForbiddenForNonOwner() {
        UUID otherUserId = UUID.randomUUID();
        when(jwt.getClaim("sub")).thenReturn(otherUserId.toString());
        when(reviewService.findReviewById(testReviewId)).thenReturn(Optional.of(mockReview));

        Response response = reviewController.updateReview(testReviewId, mockReviewRequestDTO);

        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        verify(reviewService, never()).updateReview(any());
    }

    @Test
    void testDeleteReviewShouldReturnNoContentForOwner() {
        when(reviewService.findReviewById(testReviewId)).thenReturn(Optional.of(mockReview));
        when(jwt.getClaim("sub")).thenReturn(testUserId.toString());
        doNothing().when(reviewService).deleteReviewById(testReviewId);

        Response response = reviewController.deleteReviewById(testReviewId);

        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        assertNull(response.getEntity());
        verify(reviewService, times(1)).deleteReviewById(testReviewId);
    }

    @Test
    void testDeleteReviewShouldReturnNotFoundIfReviewDoesNotExist() {
        when(reviewService.findReviewById(testReviewId)).thenReturn(Optional.empty());
        when(jwt.getClaim("sub")).thenReturn(testUserId.toString());

        Response response = reviewController.deleteReviewById(testReviewId);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertNull(response.getEntity());
        verify(reviewService, never()).deleteReviewById(any());
    }

    @Test
    void testDeleteReviewShouldReturnForbiddenForNonOwner() {
        UUID otherUserId = UUID.randomUUID();
        when(jwt.getClaim("sub")).thenReturn(otherUserId.toString());
        when(reviewService.findReviewById(testReviewId)).thenReturn(Optional.of(mockReview));

        Response response = reviewController.deleteReviewById(testReviewId);

        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        verify(reviewService, never()).deleteReviewById(any());
    }

    @Test
    void testGetReviewsByBookIdShouldReturnOkAndListOfReviewDTOs() {
        List<Review> reviews = Collections.singletonList(mockReview);
        List<ReviewResponseDTO> expectedList = Collections.singletonList(expectedResponseDTO);
        when(reviewService.getReviewsForBook(testBookId)).thenReturn(reviews);

        Response response = reviewController.getReviewsByBookId(testBookId);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(expectedList, response.getEntity());
        verify(reviewService, times(1)).getReviewsForBook(testBookId);
    }

    @Test
    void testGetReviewsByUserIdShouldReturnOkAndListOfReviewDTOs() {
        List<Review> reviews = Collections.singletonList(mockReview);
        List<ReviewResponseDTO> expectedList = Collections.singletonList(expectedResponseDTO);
        when(reviewService.getReviewsForUser(testUserId)).thenReturn(reviews);
        when(jwt.getClaim("sub")).thenReturn(testUserId.toString());

        Response response = reviewController.getReviewsByUserId(testUserId);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(expectedList, response.getEntity());
        verify(reviewService, times(1)).getReviewsForUser(testUserId);
    }

    @Test
    void testGetBookReviewStatsShouldReturnOkWithStatsForExistingBookWithReviews() {
        ReviewStats mockReviewStats = ReviewStats.builder()
                .totalReviews(5L)
                .averageRating(4.2)
                .build();
        ReviewStatsResponseDTO expectedStatsResponseDTO = ReviewStatsResponseDTO.builder()
                .bookId(testBookId.toString())
                .totalReviews(5L)
                .averageRating(4.2)
                .build();

        when(reviewService.getReviewStatsForBook(testBookId)).thenReturn(mockReviewStats);

        Response response = reviewController.getBookReviewStats(testBookId);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(expectedStatsResponseDTO, response.getEntity());
        
        verify(bookService, never()).getBookById(any());
        verify(reviewService, times(1)).getReviewStatsForBook(testBookId);
    }

    @Test
    void testGetBookReviewStatsShouldReturnOkWithZeroStatsForExistingBookWithoutReviews() {
        ReviewStats mockReviewStats = ReviewStats.builder()
                .totalReviews(0L)
                .averageRating(0.0)
                .build();
        ReviewStatsResponseDTO expectedStatsResponseDTO = ReviewStatsResponseDTO.builder()
                .bookId(testBookId.toString())
                .totalReviews(0L)
                .averageRating(0.0)
                .build();

        when(reviewService.getReviewStatsForBook(testBookId)).thenReturn(mockReviewStats);

        Response response = reviewController.getBookReviewStats(testBookId);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(expectedStatsResponseDTO, response.getEntity());
        
        verify(bookService, never()).getBookById(any());
        verify(reviewService, times(1)).getReviewStatsForBook(testBookId);
    }

    @Test
    void testGetBookReviewStatsShouldReturnNotFoundIfBookDoesNotExist() {
        when(reviewService.getReviewStatsForBook(testBookId)).thenThrow(new NotFoundException("Book not found with ID: " + testBookId));

        Response response = reviewController.getBookReviewStats(testBookId);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Book not found with ID: " + testBookId, response.getEntity());
        
        verify(bookService, never()).getBookById(any());
        verify(reviewService, times(1)).getReviewStatsForBook(testBookId);
    }

    @Test
    void testGetMyReviewForBookShouldReturnOkAndReviewDTOWhenReviewExists() {
        when(jwt.getClaim("sub")).thenReturn(testUserId.toString());
        when(bookService.getBookById(testBookId)).thenReturn(Optional.of(mockBook));
        when(userService.findUserProfileById(testUserId)).thenReturn(Optional.of(mockUser));
        when(reviewService.findReviewByUserAndBook(any(UUID.class), any(UUID.class))).thenReturn(Optional.of(mockReview));

        Response response = reviewController.getMyReviewForBook(testBookId);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(expectedResponseDTO, response.getEntity());
        verify(bookService, times(1)).getBookById(testBookId);
        verify(userService, times(1)).findUserProfileById(testUserId);
        verify(reviewService, times(1)).findReviewByUserAndBook(testUserId, testBookId);
    }

    @Test
    void testGetMyReviewForBookShouldReturnNotFoundWhenReviewDoesNotExist() {
        when(jwt.getClaim("sub")).thenReturn(testUserId.toString());
        when(bookService.getBookById(testBookId)).thenReturn(Optional.of(mockBook));
        when(userService.findUserProfileById(testUserId)).thenReturn(Optional.of(mockUser));
        when(reviewService.findReviewByUserAndBook(any(UUID.class), any(UUID.class))).thenReturn(Optional.empty());

        Response response = reviewController.getMyReviewForBook(testBookId);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Review not found for this user and book.", response.getEntity());
        verify(bookService, times(1)).getBookById(testBookId);
        verify(userService, times(1)).findUserProfileById(testUserId);
        verify(reviewService, times(1)).findReviewByUserAndBook(testUserId, testBookId);
    }

    @Test
    void testGetMyReviewForBookShouldReturnNotFoundIfBookDoesNotExist() {
        when(jwt.getClaim("sub")).thenReturn(testUserId.toString());
        when(bookService.getBookById(testBookId)).thenReturn(Optional.empty());

        Response response = reviewController.getMyReviewForBook(testBookId);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Book not found.", response.getEntity());
        verify(bookService, times(1)).getBookById(testBookId);
        verify(userService, times(1)).findUserProfileById(any());
        verify(reviewService, never()).findReviewByUserAndBook(any(), any());
    }

    @Test
    void testGetMyReviewForBookShouldReturnUnauthorizedIfAuthenticatedUserIsNotFound() {
        when(jwt.getClaim("sub")).thenReturn(testUserId.toString());
        when(bookService.getBookById(testBookId)).thenReturn(Optional.of(mockBook));
        when(userService.findUserProfileById(testUserId)).thenReturn(Optional.empty());

        Response response = reviewController.getMyReviewForBook(testBookId);

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertEquals("Authenticated user not found.", response.getEntity());
        verify(bookService, times(1)).getBookById(testBookId);
        verify(userService, times(1)).findUserProfileById(testUserId);
        verify(reviewService, never()).findReviewByUserAndBook(any(), any());
    }

    @Test
    void testGetMyReviewForBookShouldReturnInternalServerErrorOnUnexpectedException() {
        when(jwt.getClaim("sub")).thenReturn(testUserId.toString());
        when(bookService.getBookById(testBookId)).thenThrow(new RuntimeException("Database connection failed"));

        Response response = reviewController.getMyReviewForBook(testBookId);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("An unexpected error occurred: Database connection failed", response.getEntity());
        verify(bookService, times(1)).getBookById(testBookId);
        verify(userService, never()).findUserProfileById(any());
        verify(reviewService, never()).findReviewByUserAndBook(any(), any());
    }
}