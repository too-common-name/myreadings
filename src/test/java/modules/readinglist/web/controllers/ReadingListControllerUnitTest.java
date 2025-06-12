package modules.readinglist.web.controllers;

import jakarta.ws.rs.core.Response;
import modules.readinglist.core.domain.ReadingList;
import modules.readinglist.core.domain.ReadingListImpl;
import modules.readinglist.core.usecases.ReadingListService;
import modules.readinglist.web.dto.AddBookRequestDTO;
import modules.readinglist.web.dto.ReadingListRequestDTO;
import modules.readinglist.web.dto.ReadingListResponseDTO;
import modules.user.core.domain.User;
import modules.user.core.domain.UserImpl;
import modules.user.core.usecases.UserService;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReadingListControllerUnitTest {

    @InjectMocks
    private ReadingListController readingListController;

    @Mock
    private ReadingListService readingListService;

    @Mock
    private UserService userService;

    @Mock
    private JsonWebToken jwt;

    private UUID testkeycloakUserId;
    private UUID testReadingListId;
    private UUID testBookId;

    private User mockUser;
    private ReadingList mockReadingList;
    private ReadingListRequestDTO mockReadingListRequestDTO;
    private ReadingListResponseDTO expectedResponseDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testkeycloakUserId = UUID.randomUUID();
        testReadingListId = UUID.randomUUID();
        testBookId = UUID.randomUUID();

        mockUser = UserImpl.builder().keycloakUserId(testkeycloakUserId).username("testuser").build();

        mockReadingListRequestDTO = ReadingListRequestDTO.builder()
                .name("Test List")
                .description("Test Description")
                .build();

        mockReadingList = ReadingListImpl.builder()
                .readingListId(testReadingListId)
                .userId(mockUser.getKeycloakUserId())
                .name("Test List")
                .description("Test Description")
                .creationDate(LocalDateTime.now())
                .books(new ArrayList<>())
                .build();

        expectedResponseDTO = ReadingListResponseDTO.builder()
                .readingListId(testReadingListId)
                .name("Test List")
                .description("Test Description")
                .books(new ArrayList<>())
                .build();

        when(jwt.getClaim("sub")).thenReturn(testkeycloakUserId.toString());
    }

    @Test
    void testCreateReadingListShouldReturnCreatedAndDTO() {
        when(readingListService.createReadingList(any(ReadingList.class))).thenReturn(mockReadingList);
        when(userService.findUserProfileById(testkeycloakUserId)).thenReturn(Optional.of(mockUser));

        Response response = readingListController.createReadingList(mockReadingListRequestDTO);

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertEquals(expectedResponseDTO, response.getEntity());
        verify(readingListService, times(1)).createReadingList(any(ReadingList.class));
    }

    @Test
    void testCreateReadingListShouldReturnBadRequestIfUserNotFound() {
        when(userService.findUserProfileById(testkeycloakUserId)).thenReturn(Optional.empty());

        Response response = readingListController.createReadingList(mockReadingListRequestDTO);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("User not found.", response.getEntity());
        verify(readingListService, never()).createReadingList(any());
    }

    @Test
    void testGetReadingListByIdShouldReturnOkAndDTOForOwner() {
        when(readingListService.findReadingListById(testReadingListId)).thenReturn(Optional.of(mockReadingList));

        Response response = readingListController.getReadingListById(testReadingListId);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(expectedResponseDTO, response.getEntity());
        verify(readingListService, times(1)).findReadingListById(testReadingListId);
    }

    @Test
    void testGetReadingListByIdShouldReturnNotFound() {
        when(readingListService.findReadingListById(testReadingListId)).thenReturn(Optional.empty());

        Response response = readingListController.getReadingListById(testReadingListId);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        verify(readingListService, times(1)).findReadingListById(testReadingListId);
    }

    @Test
    void testGetReadingListByIdShouldReturnForbiddenForNonOwner() {
        User otherUser = UserImpl.builder().keycloakUserId(UUID.randomUUID()).username("otheruser").build();
        ReadingList otherList = ReadingListImpl.builder().readingListId(testReadingListId)
                .userId(otherUser.getKeycloakUserId()).build();
        when(readingListService.findReadingListById(testReadingListId)).thenReturn(Optional.of(otherList));

        Response response = readingListController.getReadingListById(testReadingListId);

        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        assertEquals("Reading list does not belong to the current user.", response.getEntity());
        verify(readingListService, times(1)).findReadingListById(testReadingListId);
    }

    @Test
    void testGetAllReadingListsForUserShouldReturnOkAndListOfDTOs() {
        List<ReadingList> readingLists = Collections.singletonList(mockReadingList);
        List<ReadingListResponseDTO> expectedList = Collections.singletonList(expectedResponseDTO);
        when(readingListService.getReadingListsForUser(testkeycloakUserId)).thenReturn(readingLists);

        Response response = readingListController.getAllReadingListsForUser();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(expectedList, response.getEntity());
        verify(readingListService, times(1)).getReadingListsForUser(testkeycloakUserId);
    }

    @Test
    void testUpdateReadingListShouldReturnOkAndDTOForOwner() {
        ReadingListRequestDTO updateDTO = ReadingListRequestDTO.builder().name("Updated Name")
                .description("Updated Description").build();
        ReadingList updatedList = ReadingListImpl.builder()
                .readingListId(testReadingListId)
                .userId(mockUser.getKeycloakUserId())
                .name("Updated Name")
                .description("Updated Description")
                .creationDate(LocalDateTime.now())
                .books(new ArrayList<>())
                .build();
        ReadingListResponseDTO updatedResponseDTO = ReadingListResponseDTO.builder()
                .readingListId(testReadingListId)
                .name("Updated Name")
                .description("Updated Description")
                .books(new ArrayList<>())
                .build();
        when(readingListService.findReadingListById(testReadingListId)).thenReturn(Optional.of(mockReadingList));
        when(readingListService.updateReadingList(any(ReadingList.class))).thenReturn(updatedList);

        Response response = readingListController.updateReadingList(testReadingListId, updateDTO);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(updatedResponseDTO, response.getEntity());
        verify(readingListService, times(1)).updateReadingList(any(ReadingList.class));
    }

    @Test
    void testUpdateReadingListShouldReturnNotFound() {
        when(readingListService.findReadingListById(testReadingListId)).thenReturn(Optional.empty());

        Response response = readingListController.updateReadingList(testReadingListId, mockReadingListRequestDTO);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        verify(readingListService, never()).updateReadingList(any());
    }

    @Test
    void testUpdateReadingListShouldReturnForbiddenForNonOwner() {
        User otherUser = UserImpl.builder().keycloakUserId(UUID.randomUUID()).username("otheruser").build();
        ReadingList otherList = ReadingListImpl.builder().readingListId(testReadingListId)
                .userId(otherUser.getKeycloakUserId()).build();
        when(readingListService.findReadingListById(testReadingListId)).thenReturn(Optional.of(otherList));

        Response response = readingListController.updateReadingList(testReadingListId, mockReadingListRequestDTO);

        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        assertEquals("Reading list does not belong to the current user.", response.getEntity());
        verify(readingListService, never()).updateReadingList(any());
    }

    @Test
    void testDeleteReadingListShouldReturnNoContentForOwner() {
        when(readingListService.findReadingListById(testReadingListId)).thenReturn(Optional.of(mockReadingList));
        doNothing().when(readingListService).deleteReadingListById(testReadingListId);

        Response response = readingListController.deleteReadingList(testReadingListId);

        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        verify(readingListService, times(1)).deleteReadingListById(testReadingListId);
    }

    @Test
    void testDeleteReadingListShouldReturnNotFound() {
        when(readingListService.findReadingListById(testReadingListId)).thenReturn(Optional.empty());

        Response response = readingListController.deleteReadingList(testReadingListId);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        verify(readingListService, times(1)).findReadingListById(testReadingListId);
    }

    @Test
    void testDeleteReadingListShouldReturnForbiddenForNonOwner() {
        User otherUser = UserImpl.builder().keycloakUserId(UUID.randomUUID()).username("otheruser").build();
        ReadingList otherList = ReadingListImpl.builder().readingListId(testReadingListId)
                .userId(otherUser.getKeycloakUserId()).build();
        when(readingListService.findReadingListById(testReadingListId)).thenReturn(Optional.of(otherList));

        Response response = readingListController.deleteReadingList(testReadingListId);

        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        assertEquals("Reading list does not belong to the current user.", response.getEntity());
        verify(readingListService, times(1)).findReadingListById(testReadingListId);
        verify(readingListService, never()).deleteReadingListById(testReadingListId);
    }

    @Test
    void testAddBookToReadingListShouldReturnOkForOwner() {
        when(readingListService.findReadingListById(testReadingListId)).thenReturn(Optional.of(mockReadingList));
        doNothing().when(readingListService).addBookToReadingList(testReadingListId, testBookId);

        AddBookRequestDTO addBookRequestDTO = AddBookRequestDTO.builder().bookId(testBookId).build();

        Response response = readingListController.addBookToReadingList(testReadingListId, addBookRequestDTO);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("Book added to reading list.", response.getEntity());
        verify(readingListService, times(1)).addBookToReadingList(testReadingListId, testBookId);
    }

    @Test
    void testAddBookToReadingListShouldReturnNotFoundForReadingList() {
        when(readingListService.findReadingListById(testReadingListId)).thenReturn(Optional.empty());
        AddBookRequestDTO addBookRequestDTO = AddBookRequestDTO.builder().bookId(testBookId).build();

        Response response = readingListController.addBookToReadingList(testReadingListId, addBookRequestDTO);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Reading list not found with ID: " + testReadingListId, response.getEntity());
        verify(readingListService, never()).addBookToReadingList(any(), any());
    }

    @Test
    void testAddBookToReadingListShouldReturnForbiddenForNonOwner() {
        User otherUser = UserImpl.builder().keycloakUserId(UUID.randomUUID()).username("otheruser").build();
        ReadingList otherList = ReadingListImpl.builder().readingListId(testReadingListId)
                .userId(otherUser.getKeycloakUserId()).build();
        when(readingListService.findReadingListById(testReadingListId)).thenReturn(Optional.of(otherList));
        AddBookRequestDTO addBookRequestDTO = AddBookRequestDTO.builder().bookId(testBookId).build();

        Response response = readingListController.addBookToReadingList(testReadingListId, addBookRequestDTO);

        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        assertEquals("Reading list does not belong to the current user.", response.getEntity());
        verify(readingListService, never()).addBookToReadingList(any(), any());
    }

    @Test
    void testRemoveBookFromReadingListShouldReturnNoContentForOwner() {
        when(readingListService.findReadingListById(testReadingListId)).thenReturn(Optional.of(mockReadingList));
        doNothing().when(readingListService).removeBookFromReadingList(testReadingListId, testBookId);

        Response response = readingListController.removeBookFromReadingList(testReadingListId, testBookId);

        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        verify(readingListService, times(1)).removeBookFromReadingList(testReadingListId, testBookId);
    }

    @Test
    void testRemoveBookFromReadingListShouldReturnNotFoundForReadingList() {
        when(readingListService.findReadingListById(testReadingListId)).thenReturn(Optional.empty());

        Response response = readingListController.removeBookFromReadingList(testReadingListId, testBookId);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Reading list not found with ID: " + testReadingListId, response.getEntity());
        verify(readingListService, never()).removeBookFromReadingList(any(), any());
    }

    @Test
    void testRemoveBookFromReadingListShouldReturnForbiddenForNonOwner() {
        User otherUser = UserImpl.builder().keycloakUserId(UUID.randomUUID()).username("otheruser").build();
        ReadingList otherList = ReadingListImpl.builder().readingListId(testReadingListId)
                .userId(otherUser.getKeycloakUserId()).build();
        when(readingListService.findReadingListById(testReadingListId)).thenReturn(Optional.of(otherList));

        Response response = readingListController.removeBookFromReadingList(testReadingListId, testBookId);

        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        assertEquals("Reading list does not belong to the current user.", response.getEntity());
        verify(readingListService, never()).removeBookFromReadingList(any(), any());
    }

    @Test
    void testGetBooksInReadingListShouldReturnOkAndListOfBooksForOwner() {
        List<modules.catalog.core.domain.Book> books = Collections.emptyList();
        when(readingListService.findReadingListById(testReadingListId)).thenReturn(Optional.of(mockReadingList));
        when(readingListService.getBooksInReadingList(testReadingListId)).thenReturn(books);

        Response response = readingListController.getBooksInReadingList(testReadingListId);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(books, response.getEntity());
        verify(readingListService, times(1)).getBooksInReadingList(testReadingListId);
    }

    @Test
    void testGetBooksInReadingListShouldReturnNotFoundForReadingList() {
        when(readingListService.findReadingListById(testReadingListId)).thenReturn(Optional.empty());

        Response response = readingListController.getBooksInReadingList(testReadingListId);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Reading list not found with ID: " + testReadingListId, response.getEntity());
        verify(readingListService, never()).getBooksInReadingList(any());
    }

    @Test
    void testGetBooksInReadingListShouldReturnForbiddenForNonOwner() {
        User otherUser = UserImpl.builder().keycloakUserId(UUID.randomUUID()).username("otheruser").build();
        ReadingList otherList = ReadingListImpl.builder().readingListId(testReadingListId)
                .userId(otherUser.getKeycloakUserId()).build();
        when(readingListService.findReadingListById(testReadingListId)).thenReturn(Optional.of(otherList));

        Response response = readingListController.getBooksInReadingList(testReadingListId);

        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        assertEquals("Reading list does not belong to the current user.", response.getEntity());
        verify(readingListService, never()).getBooksInReadingList(any());
    }
}