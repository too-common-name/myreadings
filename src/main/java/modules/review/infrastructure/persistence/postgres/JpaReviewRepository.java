package modules.review.infrastructure.persistence.postgres;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import modules.review.core.domain.Review;
import modules.review.core.usecases.repositories.ReviewRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import io.quarkus.arc.properties.IfBuildProperty;
import io.quarkus.hibernate.orm.PersistenceUnit;

@ApplicationScoped
@IfBuildProperty(name = "app.book.repository.type", stringValue = "jpa", enableIfMissing = true)
public class JpaReviewRepository implements ReviewRepository {

    @Inject
    @PersistenceUnit("review-db")
    EntityManager entityManager;

    @Inject
    ReviewPersistenceMapper mapper;

    @Override
    public Review create(Review review) {
        ReviewEntity newEntity = mapper.toEntity(review);
        entityManager.persist(newEntity);
        return mapper.toDomain(newEntity);
    }

    @Override
    public Review update(Review review) {
        ReviewEntity managedEntity = entityManager.find(ReviewEntity.class, review.getReviewId());

        if (managedEntity != null) {
            mapper.updateEntityFromDomain(managedEntity, review);
            return mapper.toDomain(managedEntity);
        } else {
            throw new IllegalArgumentException("Review with ID " + review.getReviewId() + " not found for update.");
        }
    }
    
    @Override
    public List<Review> saveAll(Iterable<Review> reviewsToSave) {
        List<Review> savedReviews = new ArrayList<>();
        for (Review review : reviewsToSave) {
            savedReviews.add(create(review)); 
        }
        return savedReviews;
    }

    @Override
    public Optional<Review> findById(UUID reviewId) {
        return Optional.ofNullable(entityManager.find(ReviewEntity.class, reviewId))
                .map(mapper::toDomain);
    }

    @Override
    public List<Review> findAll() {
        return entityManager.createQuery("SELECT r FROM ReviewEntity r", ReviewEntity.class)
                .getResultStream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID reviewId) {
        findById(reviewId).ifPresent(review -> {
            ReviewEntity entity = entityManager.find(ReviewEntity.class, review.getReviewId());
            if (entity != null) {
                entityManager.remove(entity);
            }
        });
    }

    @Override
    public List<Review> getBookReviews(UUID bookId) {
        String jpql = "SELECT r FROM ReviewEntity r WHERE r.bookId = :bookId";
        TypedQuery<ReviewEntity> query = entityManager.createQuery(jpql, ReviewEntity.class);
        query.setParameter("bookId", bookId);
        return query.getResultList().stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Review> getUserReviews(UUID userId) {
        String jpql = "SELECT r FROM ReviewEntity r WHERE r.userId = :userId";
        TypedQuery<ReviewEntity> query = entityManager.createQuery(jpql, ReviewEntity.class);
        query.setParameter("userId", userId);
        return query.getResultList().stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<Review> findByUserIdAndBookId(UUID userId, UUID bookId) {
        String jpql = "SELECT r FROM ReviewEntity r WHERE r.userId = :userId AND r.bookId = :bookId";
        TypedQuery<ReviewEntity> query = entityManager.createQuery(jpql, ReviewEntity.class);
        query.setParameter("userId", userId);
        query.setParameter("bookId", bookId);
        return query.getResultStream().map(mapper::toDomain).findFirst();
    }

    @Override
    public Long countReviewsByBookId(UUID bookId) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(r) FROM ReviewEntity r WHERE r.bookId = :bookId", Long.class);
        query.setParameter("bookId", bookId);
        return query.getSingleResult();
    }

    @Override
    public Double findAverageRatingByBookId(UUID bookId) {
        TypedQuery<Double> query = entityManager.createQuery(
            "SELECT AVG(r.rating) FROM ReviewEntity r WHERE r.bookId = :bookId", Double.class);
        query.setParameter("bookId", bookId);
        return query.getSingleResult();
    }
}

