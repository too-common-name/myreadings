package modules.user.core.usecases;

import java.util.UUID;

import modules.user.core.domain.User;

import java.util.Optional;

public interface UserService {

    User createUserProfile(User user);
    Optional<User> findUserProfileById(UUID userId); 
    User updateUserProfile(User user); 
    void deleteUserProfile(UUID userId);
}