package org.modular.playground.user.infrastructure.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.modular.playground.user.infrastructure.rest.dto.UserServiceResponseDTO;

import java.util.List;
import java.util.UUID;

@RegisterRestClient(configKey = "user-service-api")
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface UserServiceApi {

    @GET
    @Path("/v1/users/{userId}")
    UserServiceResponseDTO getUserById(@PathParam("userId") UUID userId,
                                       @HeaderParam("Authorization") String authHeader);

    @GET
    @Path("/internal/users/{userId}")
    UserServiceResponseDTO getInternalUserById(@PathParam("userId") UUID userId);

    @POST
    @Path("/internal/users/batch")
    List<UserServiceResponseDTO> getUsersByIds(List<UUID> userIds);
}
