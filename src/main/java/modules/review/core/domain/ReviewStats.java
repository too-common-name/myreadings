package modules.review.core.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewStats {
    private long totalReviews;
    private Double averageRating;
}
