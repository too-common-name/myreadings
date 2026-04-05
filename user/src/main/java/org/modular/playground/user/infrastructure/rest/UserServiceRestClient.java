package org.modular.playground.user.infrastructure.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.modular.playground.user.core.domain.User;
import org.modular.playground.user.core.domain.UserImpl;
import org.modular.playground.user.core.usecases.UserService;
import org.modular.playground.user.infrastructure.rest.dto.UserServiceResponseDTO;
import org.jboss.logging.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class UserServiceRestClient implements UserService {

    private static final Logger LOGGER = Logger.getLogger(UserServiceRestClient.class);

    @Inject
    @RestClient
    UserServiceApi userServiceApi;

    @Override
    public User createUserProfile(User user) {
        throw new UnsupportedOperationException(
                "User creation is handled by the standalone user-service via RabbitMQ events");
    }

    @Override
    public Optional<User> findUserProfileById(UUID userId, JsonWebToken principal) {
        try {
            String token = "Bearer " + principal.getRawToken();
            UserServiceResponseDTO dto = userServiceApi.getUserById(userId, token);
            return Optional.of(toDomain(dto));
        } catch (WebApplicationException e) {
            if (e.getResponse().getStatus() == 404) {
                return Optional.empty();
            }
            if (e.getResponse().getStatus() == 403) {
                throw new jakarta.ws.rs.ForbiddenException("User is not authorized to access this profile.");
            }
            throw e;
        }
    }

    @Override
    public Optional<User> findUserByIdInternal(UUID userId) {
        try {
            UserServiceResponseDTO dto = userServiceApi.getInternalUserById(userId);
            return Optional.of(toDomain(dto));
        } catch (WebApplicationException e) {
            if (e.getResponse().getStatus() == 404) {
                return Optional.empty();
            }
            throw e;
        }
    }

    @Override
    public List<User> findUsersByIds(List<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return userServiceApi.getUsersByIds(userIds).stream()
                    .map(this::toDomain)
                    .collect(Collectors.toList());
        } catch (WebApplicationException e) {
            LOGGER.errorf(e, "Failed to batch-fetch users from user-service");
            return Collections.emptyList();
        }
    }

    @Override
    public User updateUserProfile(User user) {
        throw new UnsupportedOperationException(
                "User update is handled by the standalone user-service");
    }

    @Override
    public void deleteUserProfile(UUID userId) {
        throw new UnsupportedOperationException(
                "User deletion is handled by the standalone user-service");
    }

    private User toDomain(UserServiceResponseDTO dto) {
        return UserImpl.builder()
                .keycloakUserId(dto.getUserId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .username(dto.getUsername())
                .email(dto.getEmail())
                .themePreference(dto.getThemePreference())
                .build();
    }
}
