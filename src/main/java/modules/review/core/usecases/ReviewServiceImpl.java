package modules.review.core.usecases;

import java.util.UUID;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import java.util.Optional;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import modules.catalog.core.domain.Book;
import modules.catalog.core.usecases.BookService;
import modules.review.core.domain.Review;
import modules.review.core.domain.ReviewImpl;
import modules.review.core.domain.ReviewStats;
import modules.review.core.usecases.repositories.ReviewRepository;
import modules.user.core.domain.User;
import modules.user.core.usecases.UserService;
import modules.review.web.dto.ReviewRequestDTO;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;
import java.time.LocalDateTime;

@ApplicationScoped
public class ReviewServiceImpl implements ReviewService {

    private static final Logger LOGGER = Logger.getLogger(ReviewServiceImpl.class);

    @Inject
    ReviewRepository reviewRepository;
    @Inject
    BookService bookService;
    @Inject
    UserService userService;

    private Review enrichReview(Review review, JsonWebToken principal) {
        if (review == null) return null;
        User fullUser = (principal != null) 
            ? userService.findUserProfileById(review.getUser().getKeycloakUserId(), principal).orElse(review.getUser()) 
            : review.getUser();
        Book fullBook = bookService.getBookById(review.getBook().getBookId())
            .orElse(review.getBook());
        return ReviewImpl.builder()
            .reviewId(review.getReviewId())
            .book(fullBook)
            .user(fullUser)
            .reviewText(review.getReviewText())
            .rating(review.getRating())
            .publicationDate(review.getPublicationDate())
            .build();
    }
    
    @Override
    public Review findReviewAndCheckOwnership(UUID reviewId, JsonWebToken principal) {
        LOGGER.debugf("Finding review %s and checking ownership for user %s", reviewId, principal.getSubject());
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review not found with ID: " + reviewId));
        
        UUID callerId = UUID.fromString(principal.getSubject());
        boolean isAdmin = false;
        if (principal.getClaim("realm_access") instanceof jakarta.json.JsonObject) {
            jakarta.json.JsonObject realmAccess = principal.getClaim("realm_access");
            jakarta.json.JsonArray roles = realmAccess.getJsonArray("roles");
            if (roles != null) {
                isAdmin = roles.stream().anyMatch(role -> "admin".equals(((jakarta.json.JsonString) role).getString()));
            }
        }
        if (!review.getUser().getKeycloakUserId().equals(callerId) && !isAdmin) {
            throw new ForbiddenException("You are not authorized to access this review.");
        }
        return enrichReview(review, principal);
    }

    @Override
    public Review createReview(ReviewRequestDTO reviewRequest, JsonWebToken principal) {
        UUID userId = UUID.fromString(principal.getSubject());
        UUID bookId = reviewRequest.getBookId();
        LOGGER.infof("Attempting to create review for book %s by user %s", bookId, userId);

        Book book = bookService.getBookById(bookId)
            .orElseThrow(() -> new IllegalArgumentException("Book not found: " + bookId));
        User user = userService.findUserProfileById(userId, principal)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Review reviewToCreate = ReviewImpl.builder()
                .reviewId(UUID.randomUUID())
                .book(book)
                .user(user)
                .reviewText(reviewRequest.getReviewText())
                .rating(reviewRequest.getRating())
                .publicationDate(LocalDateTime.now())
                .build();
        
        Review createdReview = persistNewReview(reviewToCreate);
        LOGGER.infof("Successfully created review with ID %s", createdReview.getReviewId());
        return createdReview;
    }

    @Transactional
    protected Review persistNewReview(Review review) {
        return reviewRepository.create(review);
    }

    @Override
    public Optional<Review> findReviewById(UUID reviewId, JsonWebToken principal) {
        LOGGER.debugf("Searching for review by ID: %s", reviewId);
        return reviewRepository.findById(reviewId).map(review -> enrichReview(review, principal));
    }

    @Override
    public List<Review> getReviewsForBook(UUID bookId, JsonWebToken principal) {
        LOGGER.debugf("Getting reviews for book ID: %s", bookId);
        if (bookService.getBookById(bookId).isEmpty()) {
            throw new IllegalArgumentException("Book not found: " + bookId + ". Cannot retrieve reviews.");
        }
        return reviewRepository.getBookReviews(bookId).stream()
            .map(review -> enrichReview(review, principal))
            .collect(Collectors.toList());
    }

    @Override
    public List<Review> getReviewsForUser(UUID userId, JsonWebToken principal) {
        LOGGER.debugf("Getting reviews for user ID: %s", userId);
        if (userService.findUserProfileById(userId, principal).isEmpty()) {
            throw new IllegalArgumentException("User not found: " + userId + ". Cannot retrieve reviews.");
        }
        return reviewRepository.getUserReviews(userId).stream()
            .map(review -> enrichReview(review, principal))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Review updateReview(UUID reviewId, ReviewRequestDTO reviewRequest, JsonWebToken principal) {
        LOGGER.infof("Attempting to update review with ID: %s", reviewId);
        Review existingReview = findReviewAndCheckOwnership(reviewId, principal);
        
        Review updatedReview = ReviewImpl.builder()
                .reviewId(existingReview.getReviewId())
                .book(existingReview.getBook())
                .user(existingReview.getUser())
                .publicationDate(existingReview.getPublicationDate())
                .reviewText(reviewRequest.getReviewText())
                .rating(reviewRequest.getRating())
                .build();

        return reviewRepository.update(updatedReview);
    }

    @Override
    @Transactional
    public void deleteReviewById(UUID reviewId, JsonWebToken principal) {
        LOGGER.infof("Attempting to delete review with ID: %s", reviewId);
        Review reviewToDelete = findReviewAndCheckOwnership(reviewId, principal);
        reviewRepository.deleteById(reviewToDelete.getReviewId());
    }

    @Override
    public Optional<Review> findReviewByUserAndBook(UUID userId, UUID bookId, JsonWebToken principal) {
        LOGGER.debugf("Searching for review by user ID %s and book ID %s", userId, bookId);
        if (bookService.getBookById(bookId).isEmpty()) {
            throw new IllegalArgumentException("Book not found: " + bookId + ". Cannot check user review.");
        }
        if (userService.findUserProfileById(userId, principal).isEmpty()) {
            throw new IllegalArgumentException("User not found: " + userId + ". Cannot check user review.");
        }
        return reviewRepository.findByUserIdAndBookId(userId, bookId).map(r -> enrichReview(r, principal));
    }

    @Override
    public ReviewStats getReviewStatsForBook(UUID bookId) {
        LOGGER.debugf("Getting review stats for book ID: %s", bookId);
        if (bookService.getBookById(bookId).isEmpty()) {
            throw new NotFoundException("Book not found with ID: " + bookId);
        }
        Long totalReviews = reviewRepository.countReviewsByBookId(bookId);
        Double averageRating = reviewRepository.findAverageRatingByBookId(bookId);
        if (averageRating == null) {
            averageRating = 0.0;
        }
        return ReviewStats.builder().totalReviews(totalReviews).averageRating(averageRating).build();
    }
}