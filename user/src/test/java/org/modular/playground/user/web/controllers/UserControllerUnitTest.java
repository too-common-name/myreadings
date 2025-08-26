package org.modular.playground.user.web.controllers;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Response;
import org.modular.playground.user.core.domain.User;
import org.modular.playground.user.core.domain.UserImpl;
import org.modular.playground.user.core.usecases.UserService;
import org.modular.playground.user.infrastructure.persistence.postgres.mapper.UserMapper;
import org.modular.playground.user.infrastructure.persistence.postgres.mapper.UserMapperImpl;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @Spy
    UserMapper userMapper = new UserMapperImpl();

    private UUID testUserId;
    private User mockUser;

    @BeforeEach
    void setUp() {
        userController.jwt = jwt;
        testUserId = UUID.randomUUID();
        mockUser = UserImpl.builder().keycloakUserId(testUserId).build();
    }

    @Test
    void shouldReturnOkWithUserDtoWhenUserIsFound() {
        when(userService.findUserProfileById(testUserId, jwt)).thenReturn(Optional.of(mockUser));
        Response response = userController.getUserById(testUserId);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(userService, times(1)).findUserProfileById(testUserId, jwt);
    }

    @Test
    void shouldReturnNotFoundWhenUserIsMissing() {
        when(userService.findUserProfileById(testUserId, jwt)).thenReturn(Optional.empty());
        Response response = userController.getUserById(testUserId);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        verify(userService, times(1)).findUserProfileById(testUserId, jwt);
    }

    @Test
    void shouldPropagateForbiddenExceptionWhenServiceThrowsIt() {
        UUID targetUserId = UUID.randomUUID();
        when(userService.findUserProfileById(targetUserId, jwt)).thenThrow(new ForbiddenException("Access denied."));
        assertThrows(ForbiddenException.class, () -> {
            userController.getUserById(targetUserId);
        });
        verify(userService, times(1)).findUserProfileById(targetUserId, jwt);
    }
}