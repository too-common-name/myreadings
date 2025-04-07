package modules.user.usecases;

import modules.user.core.domain.User;
import modules.user.core.domain.UserImpl;
import modules.user.core.usecases.UserServiceImpl;
import modules.user.core.usecases.repositories.UserRepository;
import modules.user.utils.UserTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    UserRepository userRepositoryMock;

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
    void testFindUserProfileByIdSuccessful() {
        UUID userId = testUser.getKeycloakUserId();
        when(userRepositoryMock.findById(userId)).thenReturn(Optional.of(testUser));

        Optional<User> foundUserOptional = userService.findUserProfileById(userId);

        assertTrue(foundUserOptional.isPresent());
        assertEquals(testUser, foundUserOptional.get());
        verify(userRepositoryMock, times(1)).findById(userId);
    }

    @Test
    void testFindUserProfileByIdFails() {
        UUID nonExistentUserId = UUID.randomUUID();
        when(userRepositoryMock.findById(nonExistentUserId)).thenReturn(Optional.empty());

        Optional<User> foundUserOptional = userService.findUserProfileById(nonExistentUserId);

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