package modules.user.utils;

import modules.user.domain.User;
import modules.user.domain.UserImpl;
import modules.user.domain.UserImpl.UserBuilder;
import java.util.Objects;

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
}