package modules.user.usecases;

import modules.user.domain.User;
import modules.user.domain.UserImpl;
import java.util.UUID;
import java.util.Optional;

public interface UserService {

    User createUserProfile(UserImpl.UserBuilder userBuilder);
    Optional<User> findUserProfileById(UUID userId); 
    User updateUserProfile(User user); 
    void deleteUserProfile(UUID userId);
}