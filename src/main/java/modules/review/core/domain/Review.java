package modules.review.core.domain;

import java.time.LocalDateTime;
import java.util.UUID;
import modules.catalog.core.domain.Book;
import modules.user.core.domain.User;

public interface Review {
    UUID getReviewId();
    Book getBook();
    User getUser();
    String getReviewText();
    int getRating();
    LocalDateTime getPublicationDate();
}