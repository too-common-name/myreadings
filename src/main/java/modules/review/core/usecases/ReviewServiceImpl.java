package modules.review.core.usecases;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.util.Optional;
import java.util.List;

import modules.catalog.core.domain.Book;
import modules.catalog.core.usecases.BookService;
import modules.review.core.domain.Review;
import modules.review.core.domain.ReviewStats;
import modules.review.core.usecases.repositories.ReviewRepository;
import modules.user.core.usecases.UserService;

@ApplicationScoped
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookService bookService;
    private final UserService userService;

    public ReviewServiceImpl(ReviewRepository reviewRepository, BookService bookService,
            UserService userService) {
        this.reviewRepository = reviewRepository;
        this.bookService = bookService;
        this.userService = userService;
    }

    @Override
    public Review createReview(Review review) {
        if (!bookService.getBookById(review.getBook().getBookId()).isPresent()) {
            throw new IllegalArgumentException("Book not found: " + review.getBook().getBookId());
        }
        if (!userService.findUserProfileById(review.getUser().getKeycloakUserId()).isPresent()) {
            throw new IllegalArgumentException("User not found: " + review.getUser().getKeycloakUserId());
        }

        return persistNewReview(review);
    }

    @Transactional
    protected Review persistNewReview(Review review) {
        return reviewRepository.create(review);
    }

    @Override
    public Optional<Review> findReviewById(UUID reviewId) {
        return reviewRepository.findById(reviewId);
    }

    @Override
    public List<Review> getReviewsForBook(UUID bookId) {
        if (!bookService.getBookById(bookId).isPresent()) {
            throw new IllegalArgumentException(
                    "Book not found: " + bookId + ". Cannot retrieve reviews.");
        }
        return reviewRepository.getBookReviews(bookId);
    }

    @Override
    public List<Review> getReviewsForUser(UUID userId) {
        if (!userService.findUserProfileById(userId).isPresent()) {
            throw new IllegalArgumentException(
                    "User not found: " + userId + ". Cannot retrieve reviews.");
        }
        return reviewRepository.getUserReviews(userId);
    }

    @Override
    @Transactional
    public Review updateReview(Review review) {
        if (reviewRepository.findById(review.getReviewId()).isEmpty()) {
            throw new IllegalArgumentException(
                    "Review not found for update: " + review.getReviewId());
        }
        return reviewRepository.update(review);
    }

    @Override
    @Transactional
    public void deleteReviewById(UUID reviewId) {
        if (reviewRepository.findById(reviewId).isEmpty()) {
            throw new IllegalArgumentException("Review not found for deletion: " + reviewId);
        }
        reviewRepository.deleteById(reviewId);
    }

    @Override
    public Optional<Review> findReviewByUserAndBook(UUID userId, UUID bookId) {
        if (!bookService.getBookById(bookId).isPresent()) {
            throw new IllegalArgumentException(
                    "Book not found: " + bookId + ". Cannot check user review.");
        }
        if (!userService.findUserProfileById(userId).isPresent()) {
            throw new IllegalArgumentException(
                    "User not found: " + userId + ". Cannot check user review.");
        }

        return reviewRepository.findByUserIdAndBookId(userId, bookId);
    }

    @Override
    public ReviewStats getReviewStatsForBook(UUID bookId) {
       Optional<Book> book = bookService.getBookById(bookId);
        if (book.isEmpty()) {
            throw new NotFoundException("Book not found with ID: " + bookId);
        }

        Long totalReviews = reviewRepository.countReviewsByBookId(bookId);
        Double averageRating = reviewRepository.findAverageRatingByBookId(bookId);
        
        if (averageRating == null) {
            averageRating = 0.0;
        }

        return ReviewStats.builder()
                .totalReviews(totalReviews)
                .averageRating(averageRating)
                .build();
    }
}
