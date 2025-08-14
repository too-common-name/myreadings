package modules.review.core.domain;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import modules.catalog.core.domain.Book;
import modules.user.core.domain.User;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ReviewImpl implements Review {

    @EqualsAndHashCode.Include
    private UUID reviewId;
    
    @NotNull
    private Book book; 
    
    @NotNull
    private User user;
    
    @Size(max = 200)
    private String reviewText;
    
    @Min(1)
    @Max(5)
    private int rating;
    
    private LocalDateTime publicationDate;

}