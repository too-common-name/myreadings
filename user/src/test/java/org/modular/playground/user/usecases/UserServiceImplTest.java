package org.modular.playground.user.usecases;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.ForbiddenException;
import org.modular.playground.user.core.domain.User;
import org.modular.playground.user.core.usecases.UserServiceImpl;
import org.modular.playground.user.core.usecases.repositories.UserRepository;
import org.modular.playground.user.utils.UserTestUtils;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    UserRepository userRepositoryMock;

    @Mock
    JsonWebToken jwt;

    @InjectMocks
    UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = UserTestUtils.createValidUser();
    }

    @Test
    void shouldCreateUserProfileSuccessfully() {
        when(userRepositoryMock.create(any(User.class))).thenReturn(testUser);

        User createdUser = userService.createUserProfile(testUser);

        assertNotNull(createdUser);
        assertEquals(testUser, createdUser);
        verify(userRepositoryMock, times(1)).create(any(User.class));
    }

    @Test
    void shouldReturnOwnUserProfileWhenRequested() {
        UUID userId = testUser.getKeycloakUserId();
        when(jwt.getSubject()).thenReturn(userId.toString());
        when(jwt.getClaim("realm_access")).thenReturn(null);
        when(userRepositoryMock.findById(userId)).thenReturn(Optional.of(testUser));

        Optional<User> foundUserOptional = userService.findUserProfileById(userId, jwt);

        assertTrue(foundUserOptional.isPresent());
        assertEquals(testUser, foundUserOptional.get());
    }

    @Test
    void shouldReturnOtherUserProfileWhenRequesterIsAdmin() {
        UUID adminId = UUID.randomUUID();
        UUID targetUserId = testUser.getKeycloakUserId();
        
        JsonObject realmAccess = Json.createObjectBuilder()
            .add("roles", Json.createArrayBuilder().add("admin").build())
            .build();

        when(jwt.getSubject()).thenReturn(adminId.toString());
        when(jwt.getClaim("realm_access")).thenReturn(realmAccess);
        when(userRepositoryMock.findById(targetUserId)).thenReturn(Optional.of(testUser));

        Optional<User> foundUserOptional = userService.findUserProfileById(targetUserId, jwt);

        assertTrue(foundUserOptional.isPresent());
        assertEquals(testUser, foundUserOptional.get());
    }

    @Test
    void shouldThrowForbiddenExceptionWhenUserRequestsOtherUserProfile() {
        UUID requesterId = UUID.randomUUID();
        UUID targetUserId = testUser.getKeycloakUserId();
        when(jwt.getSubject()).thenReturn(requesterId.toString());
        when(jwt.getClaim("realm_access")).thenReturn(null);

        assertThrows(ForbiddenException.class, () -> {
            userService.findUserProfileById(targetUserId, jwt);
        });

        verify(userRepositoryMock, never()).findById(any(UUID.class));
    }

    @Test
    void shouldReturnEmptyOptionalWhenUserProfileIsNotFound() {
        UUID nonExistentUserId = UUID.randomUUID();
        when(jwt.getSubject()).thenReturn(nonExistentUserId.toString());
        when(jwt.getClaim("realm_access")).thenReturn(null);
        when(userRepositoryMock.findById(nonExistentUserId)).thenReturn(Optional.empty());

        Optional<User> foundUserOptional = userService.findUserProfileById(nonExistentUserId, jwt);

        assertFalse(foundUserOptional.isPresent());
    }

    @Test
    void shouldUpdateUserProfileSuccessfully() {
        when(userRepositoryMock.update(testUser)).thenReturn(testUser);
        User resultUser = userService.updateUserProfile(testUser);
        assertNotNull(resultUser);
        assertEquals(testUser, resultUser);
        verify(userRepositoryMock, times(1)).update(testUser);
    }

    @Test
    void shouldDeleteUserProfileSuccessfully() {
        UUID userIdToDelete = testUser.getKeycloakUserId();
        doNothing().when(userRepositoryMock).deleteById(userIdToDelete);
        userService.deleteUserProfile(userIdToDelete);
        verify(userRepositoryMock, times(1)).deleteById(userIdToDelete);
    }

    @Test
    void shouldFindUserByIdInternalWhenUserExists() {
        when(userRepositoryMock.findById(testUser.getKeycloakUserId())).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findUserByIdInternal(testUser.getKeycloakUserId());

        assertTrue(result.isPresent());
        assertEquals(testUser.getKeycloakUserId(), result.get().getKeycloakUserId());
        verify(userRepositoryMock, times(1)).findById(testUser.getKeycloakUserId());
    }

    @Test
    void shouldFindUsersByIdsWhenIdsAreProvided() {
        List<UUID> userIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        when(userRepositoryMock.findByIds(userIds)).thenReturn(List.of(testUser));

        List<User> result = userService.findUsersByIds(userIds);

        assertNotNull(result);
        verify(userRepositoryMock, times(1)).findByIds(userIds);
    }
}