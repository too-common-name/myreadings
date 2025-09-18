package org.modular.playground.user.web.graphql;

import jakarta.inject.Inject;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;
import org.modular.playground.user.core.usecases.UserService;
import org.modular.playground.user.infrastructure.persistence.postgres.mapper.UserMapper;
import org.modular.playground.user.web.dto.UserResponseDTO;

import io.quarkus.security.Authenticated;
import io.vertx.core.cli.annotations.Description;
import jakarta.annotation.security.RolesAllowed;

import java.util.UUID;

@GraphQLApi
@Authenticated
public class UserGraphQLController {

    private static final Logger LOGGER = Logger.getLogger(UserGraphQLController.class);

    @Inject
    UserService userService;

    @Inject
    UserMapper userMapper;

    @Inject
    public JsonWebToken jwt;

    @Query("userById")
    @Description("Finds a user by their unique ID.")
    @RolesAllowed({"user", "admin"})
    public UserResponseDTO getUserById(UUID userId) {
        LOGGER.infof("Received GraphQL request to get user profile by ID: %s", userId);

        return userService.findUserProfileById(userId, jwt)
                .map(user -> {
                    LOGGER.debugf("User profile found for ID: %s", userId);
                    return userMapper.toResponseDTO(user);
                })
                .orElseGet(() -> {
                    LOGGER.warnf("User profile not found for ID: %s", userId);
                    return null;
                });
    }
}