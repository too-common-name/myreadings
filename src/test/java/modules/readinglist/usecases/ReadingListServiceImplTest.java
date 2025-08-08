package modules.readinglist.usecases;

import modules.catalog.core.domain.Book;
import modules.catalog.core.domain.BookImpl;
import modules.catalog.core.usecases.BookService;
import modules.readinglist.core.domain.ReadingList;
import modules.readinglist.core.domain.ReadingListImpl;
import modules.readinglist.core.usecases.ReadingListServiceImpl;
import modules.readinglist.core.usecases.repositories.ReadingListRepository;
import modules.readinglist.web.dto.ReadingListRequestDTO;
import modules.user.core.domain.User;
import modules.user.core.domain.UserImpl;
import modules.user.core.usecases.UserService;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReadingListServiceImplTest {

    @Mock
    private ReadingListRepository readingListRepository;

    @Mock
    private BookService bookService;

    @Mock
    private UserService userService;
    
    @Mock
    private JsonWebToken jwt;

    @InjectMocks
    private ReadingListServiceImpl readingListService;

    private User testUser;
    private Book testBook;
    private ReadingList testReadingList;
    
    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID readingListId = UUID.randomUUID();

        testUser = UserImpl.builder().keycloakUserId(userId).username("testuser").build();
        testBook = BookImpl.builder().bookId(bookId).title("Test Book").build();
        testReadingList = ReadingListImpl.builder().readingListId(readingListId).user(testUser).name("My List").build();
    }

    @Test
    void testCreateReadingListSuccessful() {
        ReadingListRequestDTO request = ReadingListRequestDTO.builder().name("New List").build();
        when(jwt.getSubject()).thenReturn(testUser.getKeycloakUserId().toString());
        when(userService.findUserProfileById(testUser.getKeycloakUserId(), jwt)).thenReturn(Optional.of(testUser));
        when(readingListRepository.create(any(ReadingList.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReadingList result = readingListService.createReadingList(request, jwt);

        assertNotNull(result);
        assertEquals("New List", result.getName());
        assertEquals(testUser.getKeycloakUserId(), result.getUser().getKeycloakUserId());
        verify(readingListRepository, times(1)).create(any(ReadingList.class));
    }

    @Test
    void testUpdateReadingListSuccessful() {
        ReadingListRequestDTO request = ReadingListRequestDTO.builder().name("Updated Name").build();
        when(jwt.getSubject()).thenReturn(testUser.getKeycloakUserId().toString());
        when(readingListRepository.findById(testReadingList.getReadingListId())).thenReturn(Optional.of(testReadingList));
        when(readingListRepository.update(any(ReadingList.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReadingList result = readingListService.updateReadingList(testReadingList.getReadingListId(), request, jwt);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        verify(readingListRepository, times(1)).update(any(ReadingList.class));
    }
    
    @Test
    void testUpdateReadingListThrowsForbiddenForNonOwner() {
        ReadingListRequestDTO request = ReadingListRequestDTO.builder().name("Updated Name").build();
        UUID otherUserId = UUID.randomUUID();
        when(jwt.getSubject()).thenReturn(otherUserId.toString());
        when(jwt.getClaim("realm_access")).thenReturn(null);
        when(readingListRepository.findById(testReadingList.getReadingListId())).thenReturn(Optional.of(testReadingList));

        assertThrows(ForbiddenException.class, () -> {
            readingListService.updateReadingList(testReadingList.getReadingListId(), request, jwt);
        });
        verify(readingListRepository, never()).update(any(ReadingList.class));
    }

    @Test
    void testDeleteReadingListByIdSuccessful() {
        when(jwt.getSubject()).thenReturn(testUser.getKeycloakUserId().toString());
        when(readingListRepository.findById(testReadingList.getReadingListId())).thenReturn(Optional.of(testReadingList));
        doNothing().when(readingListRepository).deleteById(testReadingList.getReadingListId());

        readingListService.deleteReadingListById(testReadingList.getReadingListId(), jwt);

        verify(readingListRepository, times(1)).deleteById(testReadingList.getReadingListId());
    }

    @Test
    void testAddBookToReadingListSuccessful() {
        when(jwt.getSubject()).thenReturn(testUser.getKeycloakUserId().toString());
        when(readingListRepository.findById(testReadingList.getReadingListId())).thenReturn(Optional.of(testReadingList));
        when(bookService.getBookById(testBook.getBookId())).thenReturn(Optional.of(testBook));
        doNothing().when(readingListRepository).addBookToReadingList(any(UUID.class), any(UUID.class));
        
        readingListService.addBookToReadingList(testReadingList.getReadingListId(), testBook.getBookId(), jwt);
        
        verify(readingListRepository, times(1)).addBookToReadingList(testReadingList.getReadingListId(), testBook.getBookId());
    }
    
    @Test
    void testAddBookToReadingListFailsForNonExistentBook() {
        UUID nonExistentBookId = UUID.randomUUID();
        when(jwt.getSubject()).thenReturn(testUser.getKeycloakUserId().toString());
        when(readingListRepository.findById(testReadingList.getReadingListId())).thenReturn(Optional.of(testReadingList));
        when(bookService.getBookById(nonExistentBookId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            readingListService.addBookToReadingList(testReadingList.getReadingListId(), nonExistentBookId, jwt);
        });
        verify(readingListRepository, never()).addBookToReadingList(any(), any());
    }
}