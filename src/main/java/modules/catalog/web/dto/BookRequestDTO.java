package modules.catalog.web.dto;

import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class BookRequestDTO {

    @NotBlank(message = "ISBN is required")
    private String isbn;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    private List<String> authors;

    @PastOrPresent(message = "Publication date must be in the past or present")
    private LocalDate publicationDate;

    @Size(max = 255, message = "Publisher cannot exceed 255 characters")
    private String publisher;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Min(value = 0, message = "Page count cannot be negative")
    private Integer pageCount;

    @Size(max = 255, message = "Cover image ID cannot exceed 255 characters")
    private String coverImageId;

    @Size(max = 50, message = "Original language cannot exceed 50 characters")
    private String originalLanguage;
}
