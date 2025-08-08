package modules.readinglist.web.controllers;

import jakarta.ws.rs.core.Response;
import modules.readinglist.core.domain.ReadingList;
import modules.readinglist.core.domain.ReadingListImpl;
import modules.readinglist.core.usecases.ReadingListService;
import modules.readinglist.web.dto.AddBookRequestDTO;
import modules.readinglist.web.dto.MoveBookRequestDTO;
import modules.readinglist.web.dto.ReadingListRequestDTO;
import modules.user.core.domain.User;
import modules.user.core.domain.UserImpl;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReadingListControllerUnitTest {

    @InjectMocks
    private ReadingListController readingListController;

    @Mock
    private ReadingListService readingListService;

    @Mock
    private JsonWebToken jwt;

    private UUID testReadingListId;
    private User mockUser;
    private ReadingList mockReadingList;
    private ReadingListRequestDTO mockReadingListRequestDTO;

    @BeforeEach
    void setUp() {
        readingListController.jwt = jwt;
        testReadingListId = UUID.randomUUID();
        
        mockUser = UserImpl.builder()
            .keycloakUserId(UUID.randomUUID())
            .username("testuser")
            .build();

        mockReadingListRequestDTO = ReadingListRequestDTO.builder().name("Test List").build();

        mockReadingList = ReadingListImpl.builder()
                .readingListId(testReadingListId)
                .user(mockUser)
                .name("Test List")
                .build();
    }

    @Test
    void testCreateReadingList() {
        when(readingListService.createReadingList(mockReadingListRequestDTO, jwt)).thenReturn(mockReadingList);
        Response response = readingListController.createReadingList(mockReadingListRequestDTO);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        verify(readingListService, times(1)).createReadingList(mockReadingListRequestDTO, jwt);
    }

    @Test
    void testGetReadingListById() {
        when(readingListService.findReadingListById(testReadingListId, jwt)).thenReturn(Optional.of(mockReadingList));
        Response response = readingListController.getReadingListById(testReadingListId);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(readingListService, times(1)).findReadingListById(testReadingListId, jwt);
    }

    @Test
    void testGetReadingListByIdNotFound() {
        when(readingListService.findReadingListById(testReadingListId, jwt)).thenReturn(Optional.empty());
        Response response = readingListController.getReadingListById(testReadingListId);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    void testGetAllReadingListsForUser() {
        when(jwt.getSubject()).thenReturn(mockUser.getKeycloakUserId().toString());
        when(readingListService.getReadingListsForUser(mockUser.getKeycloakUserId())).thenReturn(Collections.singletonList(mockReadingList));
        Response response = readingListController.getAllReadingListsForUser();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(readingListService, times(1)).getReadingListsForUser(mockUser.getKeycloakUserId());
    }

    @Test
    void testUpdateReadingList() {
        when(readingListService.updateReadingList(testReadingListId, mockReadingListRequestDTO, jwt)).thenReturn(mockReadingList);
        Response response = readingListController.updateReadingList(testReadingListId, mockReadingListRequestDTO);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(readingListService, times(1)).updateReadingList(testReadingListId, mockReadingListRequestDTO, jwt);
    }

    @Test
    void testDeleteReadingList() {
        doNothing().when(readingListService).deleteReadingListById(testReadingListId, jwt);
        Response response = readingListController.deleteReadingList(testReadingListId);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        verify(readingListService, times(1)).deleteReadingListById(testReadingListId, jwt);
    }

    @Test
    void testAddBookToReadingList() {
        UUID bookId = UUID.randomUUID();
        AddBookRequestDTO request = AddBookRequestDTO.builder().bookId(bookId).build();
        doNothing().when(readingListService).addBookToReadingList(testReadingListId, bookId, jwt);
        Response response = readingListController.addBookToReadingList(testReadingListId, request);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(readingListService, times(1)).addBookToReadingList(testReadingListId, bookId, jwt);
    }

    @Test
    void testRemoveBookFromReadingList() {
        UUID bookId = UUID.randomUUID();
        doNothing().when(readingListService).removeBookFromReadingList(testReadingListId, bookId, jwt);
        Response response = readingListController.removeBookFromReadingList(testReadingListId, bookId);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        verify(readingListService, times(1)).removeBookFromReadingList(testReadingListId, bookId, jwt);
    }

    @Test
    void testGetBooksInReadingList() {
        when(readingListService.getBooksInReadingList(testReadingListId, jwt)).thenReturn(Collections.emptyList());
        Response response = readingListController.getBooksInReadingList(testReadingListId);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(readingListService, times(1)).getBooksInReadingList(testReadingListId, jwt);
    }

    @Test
    void testGetReadingListForBookAndUser() {
        UUID bookId = UUID.randomUUID();
        when(jwt.getSubject()).thenReturn(mockUser.getKeycloakUserId().toString());
        when(readingListService.findReadingListForBookAndUser(mockUser.getKeycloakUserId(), bookId)).thenReturn(Optional.of(mockReadingList));
        Response response = readingListController.getReadingListForBookAndUser(bookId);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(readingListService, times(1)).findReadingListForBookAndUser(mockUser.getKeycloakUserId(), bookId);
    }

    @Test
    void testMoveBookBetweenReadingLists() {
        UUID bookId = UUID.randomUUID();
        MoveBookRequestDTO request = new MoveBookRequestDTO(UUID.randomUUID(), UUID.randomUUID());
        when(jwt.getSubject()).thenReturn(mockUser.getKeycloakUserId().toString());
        doNothing().when(readingListService).moveBookBetweenReadingLists(mockUser.getKeycloakUserId(), bookId, request.getSourceListId(), request.getTargetListId(), jwt);
        Response response = readingListController.moveBookBetweenReadingLists(bookId, request);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(readingListService).moveBookBetweenReadingLists(mockUser.getKeycloakUserId(), bookId, request.getSourceListId(), request.getTargetListId(), jwt);
    }
}