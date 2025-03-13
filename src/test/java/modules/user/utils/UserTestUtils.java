package modules.user.utils;

import modules.user.domain.UiTheme;
import modules.user.domain.User;
import modules.user.domain.UserImpl;
import modules.user.domain.UserImpl.UserBuilder;
import java.util.Objects;
import java.util.UUID;

public class UserTestUtils {

    public static UserBuilder builderFrom(User user) {
        Objects.requireNonNull(user, "User object must not be null");
        return new UserImpl.UserBuilder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail());
    }
    
    public static User createValidUser() {
        return createValidUserWithId(UUID.randomUUID());
    }

    public static User createValidUserWithId(UUID userId) {
        return new UserImpl.UserBuilder().userId(userId).firstName("Test").lastName("User")
                .username("testuser" + userId).email("test.user" + userId + "@example.com")
                .themePreference(UiTheme.LIGHT).build();
    }
}