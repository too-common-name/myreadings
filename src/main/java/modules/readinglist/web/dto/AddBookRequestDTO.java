package modules.readinglist.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AddBookRequestDTO {
    @NotNull
    private UUID bookId;
}