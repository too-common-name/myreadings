package modules.review.infrastructure.persistence.postgres;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reviews")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "review_id")
    private UUID reviewId;

    @Column(name = "book_id", nullable = false)
    private UUID bookId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "review_text", length = 200)
    private String reviewText;

    @Column(nullable = false)
    private int rating;

    @Column(name = "publication_date", nullable = false, updatable = false)
    private LocalDateTime publicationDate;
}