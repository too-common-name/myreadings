package modules.review.infrastructure.persistence.postgres;

import jakarta.enterprise.context.ApplicationScoped;
import modules.catalog.core.domain.Book;
import modules.catalog.core.domain.BookImpl;
import modules.review.core.domain.Review;
import modules.review.core.domain.ReviewImpl;
import modules.user.core.domain.User;
import modules.user.core.domain.UserImpl;

@ApplicationScoped
public class ReviewPersistenceMapper {

    public Review toDomain(ReviewEntity entity) {
        if (entity == null) {
            return null;
        }

        Book partialBook = BookImpl.builder().bookId(entity.getBookId()).build();
        User partialUser = UserImpl.builder().keycloakUserId(entity.getUserId()).build();

        return ReviewImpl.builder()
                .reviewId(entity.getReviewId())
                .book(partialBook)
                .user(partialUser)
                .reviewText(entity.getReviewText())
                .rating(entity.getRating())
                .publicationDate(entity.getPublicationDate())
                .build();
    }

    public ReviewEntity toEntity(Review domain) {
        if (domain == null) {
            return null;
        }

        return ReviewEntity.builder()
                .bookId(domain.getBook().getBookId())
                .userId(domain.getUser().getKeycloakUserId())
                .reviewText(domain.getReviewText())
                .rating(domain.getRating())
                .publicationDate(domain.getPublicationDate())
                .build();
    }

    public void updateEntityFromDomain(ReviewEntity entity, Review domain) {
        entity.setRating(domain.getRating());
        entity.setReviewText(domain.getReviewText());
    }
}