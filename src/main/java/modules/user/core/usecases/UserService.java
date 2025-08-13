package modules.user.core.usecases;

import java.util.UUID;

import org.eclipse.microprofile.jwt.JsonWebToken;

import modules.user.core.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    User createUserProfile(User user);
    Optional<User> findUserProfileById(UUID userId, JsonWebToken principal);
    Optional<User> findUserByIdInternal(UUID userId);
    List<User> findUsersByIds(List<UUID> userIds); 
    User updateUserProfile(User user); 
    void deleteUserProfile(UUID userId);
}