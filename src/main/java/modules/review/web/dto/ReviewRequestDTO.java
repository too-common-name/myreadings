package modules.review.web.dto;

import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Data
@Builder
public class ReviewRequestDTO {
    @NotNull(message = "Book ID is mandatory")
    private UUID bookId;

    @NotNull(message = "Rating is mandatory")
    @Min(1)
    @Max(5)
    private int rating;

    private String reviewText;
}