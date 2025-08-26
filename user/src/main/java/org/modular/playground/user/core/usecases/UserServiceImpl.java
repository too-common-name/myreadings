package org.modular.playground.user.core.usecases;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;
import org.modular.playground.user.core.domain.User;
import org.modular.playground.user.core.usecases.repositories.UserRepository;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = Logger.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;

    @Inject
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public User createUserProfile(User user) {
        LOGGER.infof("Creating user profile for keycloakUserId: %s, username: %s",
                user.getKeycloakUserId(), user.getUsername());
        return userRepository.create(user);
    }

    @Override
    public Optional<User> findUserProfileById(UUID userId, JsonWebToken principal) {
        LOGGER.debugf("Attempting to find user profile with ID: %s", userId);

        UUID authenticatedUserId = UUID.fromString(principal.getSubject());

        boolean isAdmin = false;
        if (principal.getClaim("realm_access") instanceof jakarta.json.JsonObject) {
            jakarta.json.JsonObject realmAccess = principal.getClaim("realm_access");
            jakarta.json.JsonArray roles = realmAccess.getJsonArray("roles");
            if (roles != null) {
                isAdmin = roles.stream().anyMatch(role -> "admin".equals(((jakarta.json.JsonString) role).getString()));
            }
        }

        if (!authenticatedUserId.equals(userId) && !isAdmin) {
            LOGGER.warnf("Authorization failed: User %s tried to access profile of user %s without admin role.",
                    authenticatedUserId, userId);
            throw new ForbiddenException("User is not authorized to access this profile.");
        }

        LOGGER.infof("User %s authorized to access profile %s (isAdmin: %s)",
                authenticatedUserId, userId, isAdmin);
        return userRepository.findById(userId);
    }

    @Override
    @Transactional
    public User updateUserProfile(User user) {
        LOGGER.infof("Updating user profile for keycloakUserId: %s", user.getKeycloakUserId());
        return userRepository.update(user);
    }

    @Override
    @Transactional
    public void deleteUserProfile(UUID userId) {
        LOGGER.infof("Deleting user profile for ID: %s", userId);
        userRepository.deleteById(userId);
    }

    @Override
    public Optional<User> findUserByIdInternal(UUID userId) {
        LOGGER.debugf("Internal search for user profile with ID: %s", userId);
        return userRepository.findById(userId);
    }

    @Override
    public List<User> findUsersByIds(List<UUID> userIds) {
        LOGGER.debugf("Searching for %d users by IDs", userIds.size());
        return userRepository.findByIds(userIds);
    }

}