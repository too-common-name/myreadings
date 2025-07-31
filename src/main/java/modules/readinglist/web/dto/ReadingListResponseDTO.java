package modules.readinglist.web.dto;

import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReadingListResponseDTO {
    private UUID readingListId;
    private String name;
    private String description;
    private List<UUID> books;
}
