package org.modular.playground.readinglist.web.controllers;

import jakarta.ws.rs.core.Response;
import org.modular.playground.readinglist.core.domain.ReadingList;
import org.modular.playground.readinglist.core.domain.ReadingListImpl;
import org.modular.playground.readinglist.core.usecases.ReadingListService;
import org.modular.playground.readinglist.infrastructure.persistence.postgres.mapper.ReadingListMapper;
import org.modular.playground.readinglist.infrastructure.persistence.postgres.mapper.ReadingListMapperImpl;
import org.modular.playground.readinglist.web.dto.AddBookRequestDTO;
import org.modular.playground.readinglist.web.dto.MoveBookRequestDTO;
import org.modular.playground.readinglist.web.dto.ReadingListRequestDTO;
import org.modular.playground.user.core.domain.User;
import org.modular.playground.user.core.domain.UserImpl;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
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

    @Spy
    private ReadingListMapper readingListMapper = new ReadingListMapperImpl();

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
    void shouldReturnCreatedWhenReadingListIsCreated() {
        when(readingListService.createReadingList(mockReadingListRequestDTO, jwt)).thenReturn(mockReadingList);
        Response response = readingListController.createReadingList(mockReadingListRequestDTO);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        verify(readingListService, times(1)).createReadingList(mockReadingListRequestDTO, jwt);
    }

    @Test
    void shouldReturnOkWhenReadingListIsFound() {
        when(readingListService.findReadingListById(testReadingListId, jwt)).thenReturn(Optional.of(mockReadingList));
        Response response = readingListController.getReadingListById(testReadingListId);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(readingListService, times(1)).findReadingListById(testReadingListId, jwt);
    }

    @Test
    void shouldReturnNotFoundWhenReadingListIsMissing() {
        when(readingListService.findReadingListById(testReadingListId, jwt)).thenReturn(Optional.empty());
        Response response = readingListController.getReadingListById(testReadingListId);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    void shouldReturnOkWithReadingListsForUser() {
        when(jwt.getSubject()).thenReturn(mockUser.getKeycloakUserId().toString());
        when(readingListService.getReadingListsForUser(mockUser.getKeycloakUserId())).thenReturn(Collections.singletonList(mockReadingList));
        Response response = readingListController.getAllReadingListsForUser();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(readingListService, times(1)).getReadingListsForUser(mockUser.getKeycloakUserId());
    }

    @Test
    void shouldReturnOkWhenReadingListIsUpdated() {
        when(readingListService.updateReadingList(testReadingListId, mockReadingListRequestDTO, jwt)).thenReturn(mockReadingList);
        Response response = readingListController.updateReadingList(testReadingListId, mockReadingListRequestDTO);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(readingListService, times(1)).updateReadingList(testReadingListId, mockReadingListRequestDTO, jwt);
    }

    @Test
    void shouldReturnNoContentWhenReadingListIsDeleted() {
        doNothing().when(readingListService).deleteReadingListById(testReadingListId, jwt);
        Response response = readingListController.deleteReadingList(testReadingListId);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        verify(readingListService, times(1)).deleteReadingListById(testReadingListId, jwt);
    }

    @Test
    void shouldReturnOkWhenBookIsAdded() {
        UUID bookId = UUID.randomUUID();
        AddBookRequestDTO request = AddBookRequestDTO.builder().bookId(bookId).build();
        doNothing().when(readingListService).addBookToReadingList(testReadingListId, bookId, jwt);
        Response response = readingListController.addBookToReadingList(testReadingListId, request);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(readingListService, times(1)).addBookToReadingList(testReadingListId, bookId, jwt);
    }

    @Test
    void shouldReturnNoContentWhenBookIsRemoved() {
        UUID bookId = UUID.randomUUID();
        doNothing().when(readingListService).removeBookFromReadingList(testReadingListId, bookId, jwt);
        Response response = readingListController.removeBookFromReadingList(testReadingListId, bookId);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        verify(readingListService, times(1)).removeBookFromReadingList(testReadingListId, bookId, jwt);
    }

    @Test
    void shouldReturnOkWithListOfBooks() {
        when(readingListService.getBooksInReadingList(testReadingListId, jwt)).thenReturn(Collections.emptyList());
        Response response = readingListController.getBooksInReadingList(testReadingListId);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(readingListService, times(1)).getBooksInReadingList(testReadingListId, jwt);
    }

    @Test
    void shouldReturnOkWhenReadingListForBookAndUserIsFound() {
        UUID bookId = UUID.randomUUID();
        when(jwt.getSubject()).thenReturn(mockUser.getKeycloakUserId().toString());
        when(readingListService.findReadingListForBookAndUser(mockUser.getKeycloakUserId(), bookId)).thenReturn(Optional.of(mockReadingList));
        Response response = readingListController.getReadingListForBookAndUser(bookId);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(readingListService, times(1)).findReadingListForBookAndUser(mockUser.getKeycloakUserId(), bookId);
    }

    @Test
    void shouldReturnOkWhenBookIsMoved() {
        UUID bookId = UUID.randomUUID();
        MoveBookRequestDTO request = new MoveBookRequestDTO(UUID.randomUUID(), UUID.randomUUID());
        when(jwt.getSubject()).thenReturn(mockUser.getKeycloakUserId().toString());
        doNothing().when(readingListService).moveBookBetweenReadingLists(mockUser.getKeycloakUserId(), bookId, request.getSourceListId(), request.getTargetListId(), jwt);
        Response response = readingListController.moveBookBetweenReadingLists(bookId, request);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(readingListService).moveBookBetweenReadingLists(mockUser.getKeycloakUserId(), bookId, request.getSourceListId(), request.getTargetListId(), jwt);
    }
}