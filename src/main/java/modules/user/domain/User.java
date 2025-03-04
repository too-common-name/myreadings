package modules.user.domain;

import java.util.UUID;

public class User {

    private final UUID keycloakUserId;
    private final String firstName;
    private final String lastName;
    private final String username;
    private final String email;
    private UiTheme themePreference;

    private User(UUID keycloakUserId, String firstName, String lastName, String username,
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
            return new User(keycloakUserId, firstName, lastName, username, email,
                    themePreference);
        }
    }

    public UUID getUserId() {
        return keycloakUserId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public UiTheme getThemePreference() {
        return themePreference;
    }

    public void updateUiTheme(UiTheme newTheme) {
        this.themePreference = newTheme;
    }

}
