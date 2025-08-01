package modules.user.web.controllers;

import jakarta.ws.rs.core.Response;
import modules.user.core.domain.User;
import modules.user.core.domain.UserImpl;
import modules.user.core.usecases.UserService;
import modules.user.web.dto.UserResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.eclipse.microprofile.jwt.JsonWebToken;
import jakarta.ws.rs.core.SecurityContext;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerUnitTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @Mock
    private JsonWebToken jwt;

    @Mock
    private SecurityContext ctx;

    private UUID testUserId;
    private UUID authenticatedUserId;

    private User mockUser;
    private UserResponseDTO expectedResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUserId = UUID.randomUUID();
        authenticatedUserId = testUserId; 

        mockUser = UserImpl.builder()
                .keycloakUserId(testUserId)
                .firstName("Daniele")
                .lastName("Rossi")
                .username("drossi")
                .email("drossi@redhat.com")
                .build();
        expectedResponse = UserResponseDTO.builder()
                .userId(testUserId)
                .firstName("Daniele")
                .lastName("Rossi")
                .username("drossi")
                .email("drossi@redhat.com")
                .build();
        when(jwt.getClaim("sub")).thenReturn(authenticatedUserId.toString());
    }

    @Test
    void testGetUserByIdShouldReturnOkAndUserDTOForSameUser() {
        when(userService.findUserProfileById(testUserId)).thenReturn(Optional.of(mockUser));

        Response response = userController.getUserById(testUserId);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(expectedResponse, response.getEntity());
        verify(userService, times(1)).findUserProfileById(testUserId);
    }

    @Test
    void testGetUserByIdShouldReturnNotFound() {
        when(userService.findUserProfileById(testUserId)).thenReturn(Optional.empty());

        Response response = userController.getUserById(testUserId);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertNull(response.getEntity());
        verify(userService, times(1)).findUserProfileById(testUserId);
    }

    @Test
    void testGetUserByIdShouldReturnForbiddenForDifferentUser() {
        UUID otherUserId = UUID.randomUUID();

        Response response = userController.getUserById(otherUserId);

        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        assertEquals("Access denied.", response.getEntity());
        verify(userService, never()).findUserProfileById(any());
    }

    @Test
    void testGetUserByIdShouldReturnOkForAdminAccessingOtherUser() {
        UUID otherUserId = UUID.randomUUID();
        when(userService.findUserProfileById(otherUserId)).thenReturn(Optional.of(mockUser));
        when(ctx.isUserInRole("admin")).thenReturn(true); // Simula utente admin

        Response response = userController.getUserById(otherUserId);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(expectedResponse, response.getEntity());
        verify(userService, times(1)).findUserProfileById(otherUserId);
    }
}