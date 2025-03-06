package modules.user.usecases;

import modules.user.domain.User;
import java.util.UUID;
import java.util.Optional;

public interface UserService {

    User createUserProfile(User user);
    Optional<User> findUserProfileById(UUID userId); 
    User updateUserProfile(User user); 
    void deleteUserProfile(UUID userId);
}