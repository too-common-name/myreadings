package modules.user.web.controllers;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import modules.user.domain.User;
import modules.user.usecases.UserService;
import modules.user.web.dto.UserResponseDTO;
import java.util.Optional;
import java.util.UUID;

@Path("/api/v1/users")
@Produces(MediaType.APPLICATION_JSON)
public class UserController {

    @Inject
    UserService userService;

    @GET
    @Path("/{userId}")
    public Response getUserById(@PathParam("userId") UUID userId) {
        Optional<User> userOptional = userService.findUserProfileById(userId);
        if (userOptional.isPresent()) {
            UserResponseDTO responseDTO = mapToUserResponseDTO(userOptional.get());
            return Response.ok(responseDTO).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    private UserResponseDTO mapToUserResponseDTO(User user) {
        return UserResponseDTO.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .email(user.getEmail())
                .themePreference(user.getThemePreference())
                .build();
    }
}