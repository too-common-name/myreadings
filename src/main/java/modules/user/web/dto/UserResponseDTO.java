package modules.user.web.dto;

import lombok.Builder;
import lombok.Data;
import modules.user.core.domain.UiTheme;

import java.util.UUID;

@Data
@Builder
public class UserResponseDTO {
    private UUID userId;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    @Builder.Default
    private UiTheme themePreference = UiTheme.LIGHT;
}