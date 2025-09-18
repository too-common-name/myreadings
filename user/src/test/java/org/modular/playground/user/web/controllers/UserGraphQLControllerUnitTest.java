package org.modular.playground.user.web.controllers;

import jakarta.ws.rs.ForbiddenException;
import org.modular.playground.user.core.domain.User;
import org.modular.playground.user.core.domain.UserImpl;
import org.modular.playground.user.core.usecases.UserService;
import org.modular.playground.user.infrastructure.persistence.postgres.mapper.UserMapper;
import org.modular.playground.user.infrastructure.persistence.postgres.mapper.UserMapperImpl;
import org.modular.playground.user.web.dto.UserResponseDTO;
import org.modular.playground.user.web.graphql.UserGraphQLController;
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserGraphQLControllerUnitTest {

    @InjectMocks
    private UserGraphQLController userGraphQLController;

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
        userGraphQLController.jwt = jwt;
        testUserId = UUID.randomUUID();
        mockUser = UserImpl.builder().keycloakUserId(testUserId).build();
    }

    @Test
    void shouldReturnOkWithUserDtoWhenUserIsFound() {
        when(userService.findUserProfileById(testUserId, jwt)).thenReturn(Optional.of(mockUser));
        UserResponseDTO result = userGraphQLController.getUserById(testUserId);
        assertNotNull(result);
        assertEquals(result.getUserId(), testUserId);
        verify(userService, times(1)).findUserProfileById(testUserId, jwt);
    }

    @Test
    void shouldReturnNotFoundWhenUserIsMissing() {
        when(userService.findUserProfileById(testUserId, jwt)).thenReturn(Optional.empty());
        UserResponseDTO result = userGraphQLController.getUserById(testUserId);
        assertNull(result);
        verify(userService, times(1)).findUserProfileById(testUserId, jwt);
    }

    @Test
    void shouldPropagateForbiddenExceptionWhenServiceThrowsIt() {
        UUID targetUserId = UUID.randomUUID();
        when(userService.findUserProfileById(targetUserId, jwt)).thenThrow(new ForbiddenException("Access denied."));
        assertThrows(ForbiddenException.class, () -> {
            userGraphQLController.getUserById(targetUserId);
        });
        verify(userService, times(1)).findUserProfileById(targetUserId, jwt);
    }
}