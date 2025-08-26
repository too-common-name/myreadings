package org.modular.playground.readinglist.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReadingListRequestDTO {
    @NotBlank
    @Size(max = 30)
    private String name;
    @Size(max = 200)
    private String description;
}
