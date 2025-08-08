package modules.user.web.controllers;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import modules.user.core.domain.User;
import modules.user.core.usecases.UserService;
import modules.user.web.dto.UserResponseDTO;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.core.SecurityContext;

@Path("/api/v1/users")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class UserController {

    private static final Logger LOGGER = Logger.getLogger(UserController.class);

    @Inject
    UserService userService;

    @Inject
    JsonWebToken jwt;

    @Context
    SecurityContext ctx;

    @GET
    @Path("/{userId}")
    @RolesAllowed({ "user", "admin" })
    public Response getUserById(@PathParam("userId") UUID userId) {
        LOGGER.infof("Received request to get user profile by ID: %s", userId);

        Optional<User> userOptional = userService.findUserProfileById(userId, jwt);

        if (userOptional.isPresent()) {
            LOGGER.debugf("User profile found for ID: %s", userId);
            UserResponseDTO responseDTO = mapToUserResponseDTO(userOptional.get());
            return Response.ok(responseDTO).build();
        } else {
            LOGGER.warnf("User profile not found for ID: %s", userId);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    private UserResponseDTO mapToUserResponseDTO(User user) {
        return UserResponseDTO.builder()
                .userId(user.getKeycloakUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .email(user.getEmail())
                .themePreference(user.getThemePreference())
                .build();
    }
}