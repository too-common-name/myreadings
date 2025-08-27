package org.modular.playground.readinglist.usecases;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modular.playground.catalog.core.domain.Book;
import org.modular.playground.catalog.core.domain.BookImpl;
import org.modular.playground.catalog.core.usecases.BookService;
import org.modular.playground.readinglist.core.domain.ReadingList;
import org.modular.playground.readinglist.core.domain.ReadingListImpl;
import org.modular.playground.readinglist.core.usecases.ReadingListServiceImpl;
import org.modular.playground.readinglist.core.usecases.repositories.ReadingListRepository;
import org.modular.playground.readinglist.infrastructure.persistence.postgres.mapper.ReadingListMapper;
import org.modular.playground.readinglist.infrastructure.persistence.postgres.mapper.ReadingListMapperImpl;
import org.modular.playground.readinglist.web.dto.ReadingListRequestDTO;
import org.modular.playground.user.core.domain.User;
import org.modular.playground.user.core.domain.UserImpl;
import org.modular.playground.user.core.usecases.UserService;

import java.util.List;
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

    @Spy
    private ReadingListMapper readingListMapper = new ReadingListMapperImpl();

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
    void shouldCreateReadingListWhenRequestIsValid() {
        ReadingListRequestDTO request = ReadingListRequestDTO.builder().name("New List").build();
        when(jwt.getSubject()).thenReturn(testUser.getKeycloakUserId().toString());
        when(userService.findUserProfileById(testUser.getKeycloakUserId(), jwt)).thenReturn(Optional.of(testUser));
        when(readingListRepository.create(any(ReadingList.class))).thenAnswer(invocation -> {
            ReadingList listToSave = invocation.getArgument(0);
            ((ReadingListImpl) listToSave).setReadingListId(UUID.randomUUID());
            return listToSave;
        });

        ReadingList result = readingListService.createReadingList(request, jwt);

        assertNotNull(result);
        assertNotNull(result.getReadingListId());
        assertEquals("New List", result.getName());
        assertEquals(testUser.getKeycloakUserId(), result.getUser().getKeycloakUserId());
        verify(readingListRepository).create(any(ReadingList.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenCreatingListForNonExistentUser() {
        ReadingListRequestDTO request = ReadingListRequestDTO.builder().name("New List").build();
        when(jwt.getSubject()).thenReturn(testUser.getKeycloakUserId().toString());
        when(userService.findUserProfileById(testUser.getKeycloakUserId(), jwt)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> readingListService.createReadingList(request, jwt));
        verify(readingListRepository, never()).create(any(ReadingList.class));
    }
    
    @Test
    void shouldCreateReadingListInternal() {
        when(readingListRepository.create(testReadingList)).thenReturn(testReadingList);
        ReadingList result = readingListService.createReadingListInternal(testReadingList);
        verify(readingListRepository).create(testReadingList);
        assertEquals(testReadingList.getName(), result.getName());
    }

    @Test
    void shouldReturnEmptyOptionalWhenFindingNonExistentListById() {
        when(readingListRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        Optional<ReadingList> result = readingListService.findReadingListById(UUID.randomUUID(), jwt);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldGetReadingListsForUser() {
        when(readingListRepository.findByUserId(testUser.getKeycloakUserId())).thenReturn(List.of(testReadingList));
        List<ReadingList> result = readingListService.getReadingListsForUser(testUser.getKeycloakUserId());
        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(readingListRepository).findByUserId(testUser.getKeycloakUserId());
    }

    @Test
    void shouldFindReadingListForBookAndUser() {
        when(readingListRepository.findReadingListContainingBookForUser(any(), any())).thenReturn(Optional.of(testReadingList));
        Optional<ReadingList> result = readingListService.findReadingListForBookAndUser(testUser.getKeycloakUserId(), testBook.getBookId());
        assertTrue(result.isPresent());
        verify(readingListRepository).findReadingListContainingBookForUser(any(), any());
    }

    @Test
    void shouldUpdateReadingListWhenUserIsOwner() {
        ReadingListRequestDTO request = ReadingListRequestDTO.builder().name("Updated Name").build();
        when(jwt.getSubject()).thenReturn(testUser.getKeycloakUserId().toString());
        when(readingListRepository.findById(testReadingList.getReadingListId())).thenReturn(Optional.of(testReadingList));
        when(readingListRepository.update(any(ReadingList.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReadingList result = readingListService.updateReadingList(testReadingList.getReadingListId(), request, jwt);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        verify(readingListRepository).update(any(ReadingList.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdatingNonExistentList() {
        ReadingListRequestDTO request = ReadingListRequestDTO.builder().name("Updated Name").build();
        UUID nonExistentListId = UUID.randomUUID();
        when(readingListRepository.findById(nonExistentListId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> readingListService.updateReadingList(nonExistentListId, request, jwt));
    }

    @Test
    void shouldThrowForbiddenExceptionWhenUpdatingAnotherUsersList() {
        ReadingListRequestDTO request = ReadingListRequestDTO.builder().name("Updated Name").build();
        UUID otherUserId = UUID.randomUUID();
        when(jwt.getSubject()).thenReturn(otherUserId.toString());
        when(jwt.getClaim("realm_access")).thenReturn(null);
        when(readingListRepository.findById(testReadingList.getReadingListId())).thenReturn(Optional.of(testReadingList));

        assertThrows(ForbiddenException.class, () -> readingListService.updateReadingList(testReadingList.getReadingListId(), request, jwt));
        verify(readingListRepository, never()).update(any(ReadingList.class));
    }

    @Test
    void shouldDeleteReadingListWhenUserIsOwner() {
        when(jwt.getSubject()).thenReturn(testUser.getKeycloakUserId().toString());
        when(readingListRepository.findById(testReadingList.getReadingListId())).thenReturn(Optional.of(testReadingList));
        doNothing().when(readingListRepository).deleteById(testReadingList.getReadingListId());

        readingListService.deleteReadingListById(testReadingList.getReadingListId(), jwt);

        verify(readingListRepository).deleteById(testReadingList.getReadingListId());
    }
    
    @Test
    void shouldThrowNotFoundExceptionWhenDeletingNonExistentList() {
        UUID nonExistentListId = UUID.randomUUID();
        when(readingListRepository.findById(nonExistentListId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> readingListService.deleteReadingListById(nonExistentListId, jwt));
    }

    @Test
    void shouldThrowForbiddenExceptionWhenDeletingAnotherUsersList() {
        UUID otherUserId = UUID.randomUUID();
        when(jwt.getSubject()).thenReturn(otherUserId.toString());
        when(jwt.getClaim("realm_access")).thenReturn(null);
        when(readingListRepository.findById(testReadingList.getReadingListId())).thenReturn(Optional.of(testReadingList));

        assertThrows(ForbiddenException.class, () -> readingListService.deleteReadingListById(testReadingList.getReadingListId(), jwt));
        verify(readingListRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void shouldAddBookToReadingListSuccessfully() {
        when(jwt.getSubject()).thenReturn(testUser.getKeycloakUserId().toString());
        when(readingListRepository.findById(testReadingList.getReadingListId())).thenReturn(Optional.of(testReadingList));
        when(bookService.getBookById(testBook.getBookId())).thenReturn(Optional.of(testBook));
        doNothing().when(readingListRepository).addBookToReadingList(any(UUID.class), any(UUID.class));

        readingListService.addBookToReadingList(testReadingList.getReadingListId(), testBook.getBookId(), jwt);

        verify(readingListRepository).addBookToReadingList(testReadingList.getReadingListId(), testBook.getBookId());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenAddingNonExistentBook() {
        UUID nonExistentBookId = UUID.randomUUID();
        when(jwt.getSubject()).thenReturn(testUser.getKeycloakUserId().toString());
        when(readingListRepository.findById(testReadingList.getReadingListId())).thenReturn(Optional.of(testReadingList));
        when(bookService.getBookById(nonExistentBookId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> readingListService.addBookToReadingList(testReadingList.getReadingListId(), nonExistentBookId, jwt));
        verify(readingListRepository, never()).addBookToReadingList(any(), any());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenAddingBookToNonExistentList() {
        UUID nonExistentListId = UUID.randomUUID();
        when(readingListRepository.findById(nonExistentListId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> readingListService.addBookToReadingList(nonExistentListId, testBook.getBookId(), jwt));
    }
    
    @Test
    void shouldRemoveBookFromReadingListWhenUserIsOwner() {
        when(jwt.getSubject()).thenReturn(testUser.getKeycloakUserId().toString());
        when(readingListRepository.findById(testReadingList.getReadingListId())).thenReturn(Optional.of(testReadingList));
        doNothing().when(readingListRepository).removeBookFromReadingList(any(UUID.class), any(UUID.class));

        readingListService.removeBookFromReadingList(testReadingList.getReadingListId(), testBook.getBookId(), jwt);

        verify(readingListRepository).removeBookFromReadingList(testReadingList.getReadingListId(), testBook.getBookId());
    }
    
    @Test
    void shouldThrowNotFoundExceptionWhenRemovingBookFromNonExistentList() {
        UUID nonExistentListId = UUID.randomUUID();
        when(readingListRepository.findById(nonExistentListId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> readingListService.removeBookFromReadingList(nonExistentListId, testBook.getBookId(), jwt));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenGettingBooksFromNonExistentList() {
        UUID nonExistentListId = UUID.randomUUID();
        when(readingListRepository.findById(nonExistentListId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> readingListService.getBooksInReadingList(nonExistentListId, jwt));
    }

    @Test
    void shouldReturnEmptyListWhenGettingBooksFromListWithNoBooks() {
        when(jwt.getSubject()).thenReturn(testUser.getKeycloakUserId().toString());
        when(readingListRepository.findById(testReadingList.getReadingListId())).thenReturn(Optional.of(testReadingList));

        List<Book> result = readingListService.getBooksInReadingList(testReadingList.getReadingListId(), jwt);

        assertTrue(result.isEmpty());
        verify(bookService, never()).getBooksByIds(any());
    }

    @Test
    void shouldMoveBookBetweenReadingLists() {
        ReadingList targetList = ReadingListImpl.builder().readingListId(UUID.randomUUID()).user(testUser).name("Target List").build();
        when(jwt.getSubject()).thenReturn(testUser.getKeycloakUserId().toString());
        when(readingListRepository.findById(testReadingList.getReadingListId())).thenReturn(Optional.of(testReadingList));
        when(readingListRepository.findById(targetList.getReadingListId())).thenReturn(Optional.of(targetList));

        readingListService.moveBookBetweenReadingLists(testUser.getKeycloakUserId(), testBook.getBookId(), testReadingList.getReadingListId(), targetList.getReadingListId(), jwt);

        verify(readingListRepository).removeBookFromReadingList(testReadingList.getReadingListId(), testBook.getBookId());
        verify(readingListRepository).addBookToReadingList(targetList.getReadingListId(), testBook.getBookId());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenMovingBookFromNonExistentSourceList() {
        UUID nonExistentListId = UUID.randomUUID();
        when(readingListRepository.findById(nonExistentListId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> readingListService.moveBookBetweenReadingLists(testUser.getKeycloakUserId(), testBook.getBookId(), nonExistentListId, testReadingList.getReadingListId(), jwt));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenMovingBookToNonExistentTargetList() {
        ReadingList targetList = ReadingListImpl.builder().readingListId(UUID.randomUUID()).user(testUser).name("Target List").build();
        when(jwt.getSubject()).thenReturn(testUser.getKeycloakUserId().toString());
        when(readingListRepository.findById(testReadingList.getReadingListId())).thenReturn(Optional.of(testReadingList));
        when(readingListRepository.findById(targetList.getReadingListId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            readingListService.moveBookBetweenReadingLists(testUser.getKeycloakUserId(), testBook.getBookId(), testReadingList.getReadingListId(), targetList.getReadingListId(), jwt);
        });

        verify(readingListRepository, never()).removeBookFromReadingList(any(), any());
        verify(readingListRepository, never()).addBookToReadingList(any(), any());
    }
}