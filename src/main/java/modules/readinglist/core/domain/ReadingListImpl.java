package modules.readinglist.core.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.Builder.Default;
import modules.catalog.core.domain.Book;
import modules.user.core.domain.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ReadingListImpl implements ReadingList {

    @EqualsAndHashCode.Include
    private UUID readingListId;
    @NotNull
    private User user;
    @NotBlank
    @Size(max = 30)
    private String name;
    @Size(max = 200)
    private String description;
    private LocalDateTime creationDate;
    @Default
    private List<Book> books = new ArrayList<>();
}