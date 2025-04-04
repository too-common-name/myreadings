package modules.catalog.core.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.Builder.Default;

@Data
@Builder
public class BookImpl implements Book {

    private final UUID bookId;
    @NotBlank
    private final String isbn;
    @NotBlank
    @Size(max = 255)
    private final String title;
    @Default
    private final List<String> authors = new ArrayList<>();
    @PastOrPresent
    private final LocalDate publicationDate;
    @Size(max = 255)
    private final String publisher;
    @Size(max = 500)
    private final String description;
    @Min(0)
    private final int pageCount;
    @Size(max = 255)
    private final String coverImageId;
    @Size(max = 50)
    private final String originalLanguage;

}
