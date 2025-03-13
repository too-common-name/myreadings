package modules.review.usecases;

import java.util.UUID;
import java.util.Optional;
import java.util.List;
import modules.catalog.usecases.BookService;
import modules.review.domain.Review;
import modules.review.infrastructure.ReviewRepository;
import modules.user.usecases.UserService;

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
        if (!userService.findUserProfileById(review.getUser().getUserId()).isPresent()) {
            throw new IllegalArgumentException("User not found: " + review.getUser().getUserId());
        }

        return reviewRepository.save(review);
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
    public Review updateReview(Review review) {
        if (reviewRepository.findById(review.getReviewId()).isEmpty()) {
            throw new IllegalArgumentException(
                    "Review not found for update: " + review.getReviewId());
        }
        return reviewRepository.save(review);
    }

    @Override
    public void deleteReviewById(UUID reviewId) {
        if (reviewRepository.findById(reviewId).isEmpty()) {
            throw new IllegalArgumentException("Review not found for deletion: " + reviewId);
        }
        reviewRepository.deleteById(reviewId);
    }

    @Override
    public double getAverageRatingForBook(UUID bookId) {
        if (!bookService.getBookById(bookId).isPresent()) {
            throw new IllegalArgumentException(
                    "Book not found: " + bookId + ". Cannot calculate average rating.");
        }
        List<Review> reviewsForBook = getReviewsForBook(bookId);

        return reviewsForBook.stream().mapToInt(Review::getRating).average().orElse(0);
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
}
