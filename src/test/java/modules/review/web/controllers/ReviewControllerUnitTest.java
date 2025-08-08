package modules.review.web.controllers;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import modules.catalog.core.domain.Book;
import modules.catalog.core.domain.BookImpl;
import modules.review.core.domain.Review;
import modules.review.core.domain.ReviewImpl;
import modules.review.core.domain.ReviewStats;
import modules.review.core.usecases.ReviewService;
import modules.review.web.dto.ReviewRequestDTO;
import modules.review.web.dto.ReviewResponseDTO;
import modules.review.web.dto.ReviewStatsResponseDTO;
import modules.user.core.domain.User;
import modules.user.core.domain.UserImpl;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewControllerUnitTest {

    @InjectMocks
    private ReviewController reviewController;

    @Mock
    private ReviewService reviewService;

    @Mock
    private JsonWebToken jwt;

    private UUID testUserId;
    private UUID testBookId;
    private UUID testReviewId;
    private Review mockReview;
    private ReviewRequestDTO mockReviewRequestDTO;
    private ReviewResponseDTO expectedResponseDTO;

    @BeforeEach
    void setUp() {
        reviewController.jwt = jwt;
        testUserId = UUID.randomUUID();
        testBookId = UUID.randomUUID();
        testReviewId = UUID.randomUUID();

        Book mockBook = BookImpl.builder().bookId(testBookId).title("Test Book").build();
        User mockUser = UserImpl.builder().keycloakUserId(testUserId).username("testuser").build();

        mockReviewRequestDTO = ReviewRequestDTO.builder()
                .bookId(testBookId)
                .rating(4)
                .reviewText("This is a test review.")
                .build();

        mockReview = ReviewImpl.builder()
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
    void testCreateReviewShouldReturnCreatedAndDTO() {
        when(reviewService.createReview(mockReviewRequestDTO, jwt)).thenReturn(mockReview);

        Response response = reviewController.createReview(mockReviewRequestDTO);

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertEquals(expectedResponseDTO, response.getEntity());
        verify(reviewService, times(1)).createReview(mockReviewRequestDTO, jwt);
    }

    @Test
    void testGetReviewByIdShouldReturnOkAndDTO() {
        when(reviewService.findReviewAndCheckOwnership(testReviewId, jwt)).thenReturn(mockReview);

        Response response = reviewController.getReviewById(testReviewId);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(expectedResponseDTO, response.getEntity());
        verify(reviewService, times(1)).findReviewAndCheckOwnership(testReviewId, jwt);
    }

    @Test
    void testGetReviewByIdShouldPropagateNotFoundException() {
        when(reviewService.findReviewAndCheckOwnership(testReviewId, jwt)).thenThrow(new NotFoundException());

        assertThrows(NotFoundException.class, () -> reviewController.getReviewById(testReviewId));

        verify(reviewService, times(1)).findReviewAndCheckOwnership(testReviewId, jwt);
    }

    @Test
    void testUpdateReviewShouldReturnOkAndDTO() {
        when(reviewService.updateReview(testReviewId, mockReviewRequestDTO, jwt)).thenReturn(mockReview);

        Response response = reviewController.updateReview(testReviewId, mockReviewRequestDTO);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(expectedResponseDTO, response.getEntity());
        verify(reviewService, times(1)).updateReview(testReviewId, mockReviewRequestDTO, jwt);
    }

    @Test
    void testDeleteReviewByIdShouldReturnNoContent() {
        doNothing().when(reviewService).deleteReviewById(testReviewId, jwt);

        Response response = reviewController.deleteReviewById(testReviewId);

        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        verify(reviewService, times(1)).deleteReviewById(testReviewId, jwt);
    }

    @Test
    void testDeleteReviewShouldPropagateForbiddenException() {
        doThrow(new ForbiddenException()).when(reviewService).deleteReviewById(testReviewId, jwt);

        assertThrows(ForbiddenException.class, () -> reviewController.deleteReviewById(testReviewId));

        verify(reviewService, times(1)).deleteReviewById(testReviewId, jwt);
    }

    @Test
    void testGetReviewsByBookIdShouldReturnOkAndListOfDTOs() {
        List<Review> reviews = Collections.singletonList(mockReview);
        List<ReviewResponseDTO> expectedList = Collections.singletonList(expectedResponseDTO);
        when(reviewService.getReviewsForBook(testBookId)).thenReturn(reviews);

        Response response = reviewController.getReviewsByBookId(testBookId);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(expectedList, response.getEntity());
        verify(reviewService, times(1)).getReviewsForBook(testBookId);
    }

    @Test
    void testGetReviewsByUserIdShouldReturnOkAndListOfDTOs() {
        List<Review> reviews = Collections.singletonList(mockReview);
        List<ReviewResponseDTO> expectedList = Collections.singletonList(expectedResponseDTO);
        when(reviewService.getReviewsForUser(testUserId, jwt)).thenReturn(reviews);

        Response response = reviewController.getReviewsByUserId(testUserId);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(expectedList, response.getEntity());
        verify(reviewService, times(1)).getReviewsForUser(testUserId, jwt);
    }

    @Test
    void testGetBookReviewStatsShouldReturnOkWithStats() {
        ReviewStats mockReviewStats = ReviewStats.builder().totalReviews(5L).averageRating(4.2).build();
        ReviewStatsResponseDTO expectedStatsResponseDTO = ReviewStatsResponseDTO.builder()
                .bookId(testBookId.toString()).totalReviews(5L).averageRating(4.2).build();
        when(reviewService.getReviewStatsForBook(testBookId)).thenReturn(mockReviewStats);

        Response response = reviewController.getBookReviewStats(testBookId);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(expectedStatsResponseDTO, response.getEntity());
        verify(reviewService, times(1)).getReviewStatsForBook(testBookId);
    }

    @Test
    void testGetMyReviewForBookShouldReturnOkAndDTO() {
        when(jwt.getSubject()).thenReturn(testUserId.toString());
        when(reviewService.findReviewByUserAndBook(testUserId, testBookId, jwt)).thenReturn(Optional.of(mockReview));

        Response response = reviewController.getMyReviewForBook(testBookId);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(expectedResponseDTO, response.getEntity());
        verify(reviewService, times(1)).findReviewByUserAndBook(testUserId, testBookId, jwt);
    }

    @Test
    void testGetMyReviewForBookShouldThrowNotFound() {
        when(jwt.getSubject()).thenReturn(testUserId.toString());
        when(reviewService.findReviewByUserAndBook(testUserId, testBookId, jwt)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> reviewController.getMyReviewForBook(testBookId));

        verify(reviewService, times(1)).findReviewByUserAndBook(testUserId, testBookId, jwt);
    }
}