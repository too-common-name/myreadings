package modules.readinglist.web.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MoveBookRequestDTO {
    @NotNull
    private UUID sourceListId;
    @NotNull
    private UUID targetListId;
}