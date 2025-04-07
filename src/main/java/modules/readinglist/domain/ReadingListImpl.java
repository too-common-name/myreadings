package modules.readinglist.domain;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import modules.catalog.core.domain.Book;
import modules.user.core.domain.User;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReadingListImpl implements ReadingList {

    private final UUID readingListId;
    @NotNull
    private final User user;
    @NotBlank
    @Size(max = 30)
    private final String name;
    @Size(max = 200)
    private final String description;
    private final LocalDateTime creationDate;
    @Builder.Default
    private final List<Book> books = new ArrayList<>(); 
    
}
