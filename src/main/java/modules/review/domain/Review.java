package modules.review.domain;

import java.time.LocalDateTime;
import java.util.UUID;
import modules.catalog.domain.Book;
import modules.user.domain.User;


public interface Review {

    UUID getReviewId();

    Book getBook();

    User getUser();

    String getReviewText();

    int getRating();

    LocalDateTime getPublicationDate();

}
