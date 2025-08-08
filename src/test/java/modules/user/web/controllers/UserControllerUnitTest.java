package modules.user.web.controllers;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ForbiddenException;
import modules.user.core.domain.User;
import modules.user.core.domain.UserImpl;
import modules.user.core.usecases.UserService;
import modules.user.web.dto.UserResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerUnitTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @Mock
    private JsonWebToken jwt;

    private UUID testUserId;
    private User mockUser;
    private UserResponseDTO expectedResponse;

    @BeforeEach
    void setUp() {
        userController.jwt = jwt;
        testUserId = UUID.randomUUID();

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
    }

    @Test
    void testGetUserByIdShouldReturnOkWhenUserFound() {
        when(userService.findUserProfileById(testUserId, jwt)).thenReturn(Optional.of(mockUser));

        Response response = userController.getUserById(testUserId);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(expectedResponse, response.getEntity());
        verify(userService, times(1)).findUserProfileById(testUserId, jwt);
    }

    @Test
    void testGetUserByIdShouldReturnNotFoundWhenUserNotPresent() {
        when(userService.findUserProfileById(testUserId, jwt)).thenReturn(Optional.empty());

        Response response = userController.getUserById(testUserId);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        verify(userService, times(1)).findUserProfileById(testUserId, jwt);
    }

    @Test
    void testGetUserByIdShouldPropagateForbiddenExceptionFromService() {
        UUID targetUserId = UUID.randomUUID();
        when(userService.findUserProfileById(targetUserId, jwt)).thenThrow(new ForbiddenException("Access denied."));

        assertThrows(ForbiddenException.class, () -> {
            userController.getUserById(targetUserId);
        });

        verify(userService, times(1)).findUserProfileById(targetUserId, jwt);
    }
}