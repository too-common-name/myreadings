package modules.user.domain;

import java.util.UUID;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserImpl implements User {

    private final UUID keycloakUserId;
    @NotBlank
    @Size(max = 30)
    private final String firstName;
    @NotBlank
    @Size(max = 30)
    private final String lastName;
    @NotBlank
    @Size(max = 30)
    private final String username;
    @NotBlank
    @Email
    private final String email;
    private UiTheme themePreference;

    private UserImpl(UUID keycloakUserId, String firstName, String lastName, String username,
            String email, UiTheme themePreference) {
        this.keycloakUserId = keycloakUserId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.themePreference = themePreference;
    }

    public static class UserBuilder {
        private UUID keycloakUserId;
        private String firstName;
        private String lastName;
        private String username;
        private String email;
        private UiTheme themePreference = UiTheme.LIGHT;

        public UserBuilder userId(UUID keycloakUserId) {
            this.keycloakUserId = keycloakUserId;
            return this;
        }

        public UserBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public UserBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public UserBuilder username(String username) {
            this.username = username;
            return this;
        }

        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder themePreference(UiTheme themePreference) {
            this.themePreference = themePreference;
            return this;
        }

        public User build() {
            // Validations will be performed by Keycloak
            return new UserImpl(keycloakUserId, firstName, lastName, username, email,
                    themePreference);
        }
    }

    
    @Override
    public UUID getUserId() {
        return keycloakUserId;
    }

    
    @Override
    public String getFirstName() {
        return firstName;
    }

    
    @Override
    public String getLastName() {
        return lastName;
    }

    
    @Override
    public String getUsername() {
        return username;
    }

    
    @Override
    public String getEmail() {
        return email;
    }

    
    @Override
    public UiTheme getThemePreference() {
        return themePreference;
    }
    
    
    @Override
    public void updateUiTheme(UiTheme newTheme) {
        this.themePreference = newTheme;
    }

}
