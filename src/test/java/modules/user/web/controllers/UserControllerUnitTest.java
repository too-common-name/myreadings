package modules.user.web.controllers;

import jakarta.ws.rs.core.Response;
import modules.user.domain.User;
import modules.user.domain.UserImpl;
import modules.user.usecases.UserService;
import modules.user.web.dto.UserResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUserByIdShouldReturnOkAndUserDTO() {
        UUID userId = UUID.randomUUID();
        User mockUser = new UserImpl.UserBuilder()
                .userId(userId)
                .firstName("Daniele")
                .lastName("Rossi")
                .username("drossi")
                .email("drossi@redhat.com")
                .build();
        UserResponseDTO expectedResponse = UserResponseDTO.builder()
                .userId(userId)
                .firstName("Daniele")
                .lastName("Rossi")
                .username("drossi")
                .email("drossi@redhat.com")
                .build();
        when(userService.findUserProfileById(userId)).thenReturn(Optional.of(mockUser));

        Response response = userController.getUserById(userId);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(expectedResponse, response.getEntity());
        verify(userService, times(1)).findUserProfileById(userId);
    }

    @Test
    void testGetUserByIdShouldReturnNotFound() {
        UUID userId = UUID.randomUUID();
        when(userService.findUserProfileById(userId)).thenReturn(Optional.empty());
        Response response = userController.getUserById(userId);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertNull(response.getEntity());
        verify(userService, times(1)).findUserProfileById(userId);
    }
}