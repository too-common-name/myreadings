package org.modular.playground.review.web.controllers;

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
import org.modular.playground.review.core.domain.Review;
import org.modular.playground.review.core.domain.ReviewImpl;
import org.modular.playground.review.core.domain.ReviewStatsImpl;
import org.modular.playground.review.core.usecases.ReviewService;
import org.modular.playground.review.infrastructure.persistence.postgres.mapper.ReviewMapper;
import org.modular.playground.review.infrastructure.persistence.postgres.mapper.ReviewMapperImpl;
import org.modular.playground.review.web.dto.ReviewRequestDTO;
import org.modular.playground.review.web.dto.ReviewResponseDTO;
import org.modular.playground.review.web.dto.ReviewStatsResponseDTO;
import org.modular.playground.review.web.graphql.ReviewGraphQLController;
import org.modular.playground.user.core.domain.User;
import org.modular.playground.user.core.domain.UserImpl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewGraphQLControllerUnitTest {

    @InjectMocks
    private ReviewGraphQLController reviewGraphQLController;

    @Mock
    private ReviewService reviewService;

    @Mock
    private JsonWebToken jwt;

    @Spy
    private ReviewMapper reviewMapper = new ReviewMapperImpl();

    private UUID testUserId;
    private UUID testBookId;
    private UUID testReviewId;
    private Review mockReview;
    private ReviewRequestDTO mockReviewRequestDTO;
    private ReviewResponseDTO expectedResponseDTO;

    @BeforeEach
    void setUp() {
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
    void shouldReturnDtoWhenReviewIsCreated() {
        when(reviewService.createReview(mockReviewRequestDTO, jwt)).thenReturn(mockReview);
        ReviewResponseDTO result = reviewGraphQLController.createReview(mockReviewRequestDTO);
        assertEquals(expectedResponseDTO, result);
        verify(reviewService, times(1)).createReview(mockReviewRequestDTO, jwt);
    }

    @Test
    void shouldReturnDtoWhenReviewIsFound() {
        when(reviewService.findReviewAndCheckOwnership(testReviewId, jwt)).thenReturn(mockReview);
        ReviewResponseDTO result = reviewGraphQLController.reviewById(testReviewId);
        assertEquals(expectedResponseDTO, result);
        verify(reviewService, times(1)).findReviewAndCheckOwnership(testReviewId, jwt);
    }

    @Test
    void shouldPropagateNotFoundExceptionWhenReviewIsMissing() {
        when(reviewService.findReviewAndCheckOwnership(testReviewId, jwt)).thenThrow(new NotFoundException());
        assertThrows(NotFoundException.class, () -> reviewGraphQLController.reviewById(testReviewId));
        verify(reviewService, times(1)).findReviewAndCheckOwnership(testReviewId, jwt);
    }

    @Test
    void shouldReturnDtoWhenReviewIsUpdated() {
        when(reviewService.updateReview(testReviewId, mockReviewRequestDTO, jwt)).thenReturn(mockReview);
        ReviewResponseDTO result = reviewGraphQLController.updateReview(testReviewId, mockReviewRequestDTO);
        assertEquals(expectedResponseDTO, result);
        verify(reviewService, times(1)).updateReview(testReviewId, mockReviewRequestDTO, jwt);
    }

    @Test
    void shouldReturnTrueWhenReviewIsDeleted() {
        doNothing().when(reviewService).deleteReviewById(testReviewId, jwt);
        boolean result = reviewGraphQLController.deleteReview(testReviewId);
        assertTrue(result);
        verify(reviewService, times(1)).deleteReviewById(testReviewId, jwt);
    }

    @Test
    void shouldPropagateForbiddenExceptionWhenDeletingReview() {
        doThrow(new ForbiddenException()).when(reviewService).deleteReviewById(testReviewId, jwt);
        assertThrows(ForbiddenException.class, () -> reviewGraphQLController.deleteReview(testReviewId));
        verify(reviewService, times(1)).deleteReviewById(testReviewId, jwt);
    }

    @Test
    void shouldReturnListOfDtosWhenGettingReviewsByBookId() {
        List<Review> reviews = Collections.singletonList(mockReview);
        List<ReviewResponseDTO> expectedList = Collections.singletonList(expectedResponseDTO);
        when(reviewService.getReviewsForBook(testBookId, jwt)).thenReturn(reviews);
        List<ReviewResponseDTO> result = reviewGraphQLController.reviewsByBookId(testBookId);
        assertEquals(expectedList, result);
        verify(reviewService, times(1)).getReviewsForBook(testBookId, jwt);
    }

    @Test
    void shouldReturnListOfDtosWhenGettingReviewsByUserId() {
        List<Review> reviews = Collections.singletonList(mockReview);
        List<ReviewResponseDTO> expectedList = Collections.singletonList(expectedResponseDTO);
        when(reviewService.getReviewsForUser(testUserId, jwt)).thenReturn(reviews);
        List<ReviewResponseDTO> result = reviewGraphQLController.reviewsByUserId(testUserId);
        assertEquals(expectedList, result);
        verify(reviewService, times(1)).getReviewsForUser(testUserId, jwt);
    }

    @Test
    void shouldReturnStatsWhenGettingBookReviewStats() {
        ReviewStatsImpl mockReviewStats = ReviewStatsImpl.builder().totalReviews(5L).averageRating(4.2).build();
        ReviewStatsResponseDTO expectedStatsResponseDTO = ReviewStatsResponseDTO.builder()
                .bookId(testBookId.toString()).totalReviews(5L).averageRating(4.2).build();
        when(reviewService.getReviewStatsForBook(testBookId)).thenReturn(mockReviewStats);
        ReviewStatsResponseDTO result = reviewGraphQLController.reviewStatsByBookId(testBookId);
        assertEquals(expectedStatsResponseDTO, result);
        verify(reviewService, times(1)).getReviewStatsForBook(testBookId);
    }

    @Test
    void shouldReturnDtoWhenGettingMyReviewForBook() {
        when(jwt.getSubject()).thenReturn(testUserId.toString());
        when(reviewService.findReviewByUserAndBook(testUserId, testBookId, jwt)).thenReturn(Optional.of(mockReview));
        ReviewResponseDTO result = reviewGraphQLController.myReviewForBook(testBookId);
        assertEquals(expectedResponseDTO, result);
        verify(reviewService, times(1)).findReviewByUserAndBook(testUserId, testBookId, jwt);
    }

    @Test
    void shouldReturnNullWhenGettingMyReviewForBookThatDoesNotExist() {
        when(jwt.getSubject()).thenReturn(testUserId.toString());
        when(reviewService.findReviewByUserAndBook(testUserId, testBookId, jwt)).thenReturn(Optional.empty());
        ReviewResponseDTO result = reviewGraphQLController.myReviewForBook(testBookId);
        assertNull(result);
        verify(reviewService, times(1)).findReviewByUserAndBook(testUserId, testBookId, jwt);
    }
}