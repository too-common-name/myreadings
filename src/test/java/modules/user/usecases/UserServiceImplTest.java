package modules.user.usecases;

import modules.user.core.domain.User;
import modules.user.core.domain.UserImpl;
import modules.user.core.usecases.UserServiceImpl;
import modules.user.core.usecases.repositories.UserRepository;
import modules.user.utils.UserTestUtils;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.ForbiddenException;
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
        testUser = UserImpl.builder()
                .keycloakUserId(UUID.randomUUID())
                .username("testuser")
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .build();
    }

    @Test
    void testCreateUserProfileSuccessful() {
        when(userRepositoryMock.save(any(User.class))).thenReturn(testUser);

        User createdUser = userService.createUserProfile(UserTestUtils.builderFrom(testUser).build());

        assertNotNull(createdUser);
        assertEquals(testUser, createdUser);
        verify(userRepositoryMock, times(1)).save(any(User.class));
    }

    @Test
    void testFindOwnUserProfileSuccessful() {
        UUID userId = testUser.getKeycloakUserId();
        when(jwt.getSubject()).thenReturn(userId.toString());
        when(jwt.getClaim("realm_access")).thenReturn(null);
        when(userRepositoryMock.findById(userId)).thenReturn(Optional.of(testUser));

        Optional<User> foundUserOptional = userService.findUserProfileById(userId, jwt);

        assertTrue(foundUserOptional.isPresent());
        assertEquals(testUser, foundUserOptional.get());
        verify(userRepositoryMock, times(1)).findById(userId);
    }

    @Test
    void testFindOtherUserProfileAsAdminSuccessful() {
        UUID adminId = UUID.randomUUID();
        UUID targetUserId = testUser.getKeycloakUserId();
        
        JsonObject realmAccess = Json.createObjectBuilder()
            .add("roles", Json.createArrayBuilder().add("admin").add("user").build())
            .build();

        when(jwt.getSubject()).thenReturn(adminId.toString());
        when(jwt.getClaim("realm_access")).thenReturn(realmAccess);
        when(userRepositoryMock.findById(targetUserId)).thenReturn(Optional.of(testUser));

        Optional<User> foundUserOptional = userService.findUserProfileById(targetUserId, jwt);

        assertTrue(foundUserOptional.isPresent());
        assertEquals(testUser, foundUserOptional.get());
        verify(userRepositoryMock, times(1)).findById(targetUserId);
    }

    @Test
    void testFindOtherUserProfileAsUserThrowsForbiddenException() {
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
    void testFindUserProfileByIdReturnsEmptyWhenUserNotFound() {
        UUID nonExistentUserId = UUID.randomUUID();
        when(jwt.getSubject()).thenReturn(nonExistentUserId.toString());
        when(jwt.getClaim("realm_access")).thenReturn(null);
        when(userRepositoryMock.findById(nonExistentUserId)).thenReturn(Optional.empty());

        Optional<User> foundUserOptional = userService.findUserProfileById(nonExistentUserId, jwt);

        assertFalse(foundUserOptional.isPresent());
        verify(userRepositoryMock, times(1)).findById(nonExistentUserId);
    }

    @Test
    void testUpdateUserProfileSuccessful() {
        User updatedUser = UserTestUtils.builderFrom(testUser)
                .firstName("UpdatedFirstName")
                .build();
        when(userRepositoryMock.save(updatedUser)).thenReturn(updatedUser);

        User resultUser = userService.updateUserProfile(updatedUser);

        assertNotNull(resultUser);
        assertEquals(updatedUser, resultUser);
        verify(userRepositoryMock, times(1)).save(updatedUser);
    }

    @Test
    void testDeleteUserProfileSuccessful() {
        UUID userIdToDelete = testUser.getKeycloakUserId();
        doNothing().when(userRepositoryMock).deleteById(userIdToDelete);

        userService.deleteUserProfile(userIdToDelete);

        verify(userRepositoryMock, times(1)).deleteById(userIdToDelete);
    }
}