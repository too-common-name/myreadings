package modules.user.core.domain;

import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserImpl implements User {

    private final UUID keycloakUserId;
    @NotBlank
    @Size(max = 50)
    private final String firstName;
    @NotBlank
    @Size(max = 50)
    private final String lastName;
    @NotBlank
    @Size(max = 80)
    private final String username;
    @NotBlank
    @Email
    @Size(max = 100)
    private final String email;
    @Builder.Default
    private UiTheme themePreference = UiTheme.LIGHT;
  
}
