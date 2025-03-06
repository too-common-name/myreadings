package modules.review.domain;

import java.time.LocalDateTime;
import java.util.UUID;
import common.annotations.Generated;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import modules.user.domain.User;
import modules.catalog.domain.Book;

public class ReviewImpl implements Review {

    private final UUID reviewId;
    @NotNull
    private final Book book; 
    @NotNull
    private final User user;
    @Size(max = 200)
    private final String reviewText;
    @Min(1)
    @Max(5)
    private final int rating;
    private final LocalDateTime publicationDate;

    private ReviewImpl(UUID reviewId, Book book, User user, String reviewText, int rating,
                       LocalDateTime publicationDate) {
        this.reviewId = reviewId;
        this.book = book;
        this.user = user;
        this.reviewText = reviewText;
        this.rating = rating;
        this.publicationDate = publicationDate;
    }

    public static class ReviewBuilder {
        private UUID reviewId;
        private Book book;
        private User user;
        private String reviewText;
        private int rating;
        private LocalDateTime publicationDate;

        public ReviewBuilder reviewId(UUID reviewId) {
            this.reviewId = reviewId;
            return this;
        }

        public ReviewBuilder book(Book book) {
            this.book = book;
            return this;
        }

        public ReviewBuilder user(User user) {
            this.user = user;
            return this;
        }

        public ReviewBuilder reviewText(String reviewText) {
            this.reviewText = reviewText;
            return this;
        }

        public ReviewBuilder rating(int rating) {
            this.rating = rating;
            return this;
        }

        public ReviewBuilder publicationDate(LocalDateTime publicationDate) {
            this.publicationDate = publicationDate;
            return this;
        }

        public Review build() {
            return new ReviewImpl(reviewId, book, user, reviewText, rating, publicationDate);
        }
    }

    @Generated
    @Override
    public UUID getReviewId() {
        return reviewId;
    }

    @Generated
    @Override
    public Book getBook() {
        return book;
    }

    @Generated
    @Override
    public User getUser() {
        return user;
    }

    @Generated
    @Override
    public String getReviewText() {
        return reviewText;
    }

    @Generated
    @Override
    public int getRating() {
        return rating;
    }
    
    @Generated
    @Override
    public LocalDateTime getPublicationDate() {
        return publicationDate;
    }
}