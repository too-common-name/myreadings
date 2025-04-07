package modules.user.utils;

import java.util.Objects;
import java.util.UUID;

import modules.user.core.domain.UiTheme;
import modules.user.core.domain.User;
import modules.user.core.domain.UserImpl;

public class UserTestUtils {

    public static UserImpl.UserImplBuilder builderFrom(User user) {
        Objects.requireNonNull(user, "User object must not be null");
        return UserImpl.builder()
                .keycloakUserId(user.getKeycloakUserId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail());
    }
    
    public static User createValidUser() {
        return createValidUserWithId(UUID.randomUUID());
    }

    public static User createValidUserWithId(UUID userId) {
        return UserImpl.builder().keycloakUserId(userId).firstName("Test").lastName("User")
                .username("testuser" + userId).email("test.user" + userId + "@example.com")
                .themePreference(UiTheme.LIGHT).build();
    }
}