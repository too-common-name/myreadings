package org.modular.playground.review.utils;

import java.util.UUID;

import org.modular.playground.review.core.domain.Review;
import org.modular.playground.review.core.usecases.repositories.ReviewRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ReviewRepositoryUtils {

    @Inject
    ReviewRepository reviewRepository;

    @Transactional
    public Review saveReview(Review review) {
        return reviewRepository.create(review);
    }

    @Transactional
    public void deleteReview(UUID id) {
        try {
            reviewRepository.deleteById(id);
        } catch (Exception e) {
        }
    }

}
