package org.modular.playground.readinglist.web.controllers;

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
import org.modular.playground.catalog.infrastructure.persistence.postgres.mapper.BookMapper;
import org.modular.playground.catalog.infrastructure.persistence.postgres.mapper.BookMapperImpl;
import org.modular.playground.catalog.web.dto.BookResponseDTO;
import org.modular.playground.readinglist.core.domain.ReadingList;
import org.modular.playground.readinglist.core.domain.ReadingListImpl;
import org.modular.playground.readinglist.core.usecases.ReadingListService;
import org.modular.playground.readinglist.infrastructure.persistence.postgres.mapper.ReadingListMapper;
import org.modular.playground.readinglist.infrastructure.persistence.postgres.mapper.ReadingListMapperImpl;
import org.modular.playground.readinglist.web.dto.ReadingListRequestDTO;
import org.modular.playground.readinglist.web.dto.ReadingListResponseDTO;
import org.modular.playground.readinglist.web.graphql.ReadingGraphQLController;
import org.modular.playground.user.core.domain.User;
import org.modular.playground.user.core.domain.UserImpl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReadingGraphQLControllerUnitTest {

    @InjectMocks
    private ReadingGraphQLController readingGraphQLController;

    @Mock
    private ReadingListService readingListService;

    @Mock
    private JsonWebToken jwt;

    @Spy
    private ReadingListMapper readingListMapper = new ReadingListMapperImpl();

    @Spy
    private BookMapper bookMapper = new BookMapperImpl();

    private UUID testReadingListId;
    private User mockUser;
    private ReadingList mockReadingList;
    private ReadingListRequestDTO mockReadingListRequestDTO;
    private ReadingListResponseDTO expectedReadingListResponseDTO;

    @BeforeEach
    void setUp() {
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
        
        expectedReadingListResponseDTO = readingListMapper.toResponseDTO(mockReadingList);
    }

    @Test
    void shouldReturnDtoWhenReadingListIsCreated() {
        when(readingListService.createReadingList(mockReadingListRequestDTO, jwt)).thenReturn(mockReadingList);
        ReadingListResponseDTO result = readingGraphQLController.createReadingList(mockReadingListRequestDTO);
        assertEquals(expectedReadingListResponseDTO, result);
        verify(readingListService, times(1)).createReadingList(mockReadingListRequestDTO, jwt);
    }

    @Test
    void shouldReturnDtoWhenReadingListIsFound() {
        when(readingListService.findReadingListById(testReadingListId, jwt)).thenReturn(Optional.of(mockReadingList));
        ReadingListResponseDTO result = readingGraphQLController.readingListById(testReadingListId);
        assertEquals(expectedReadingListResponseDTO, result);
        verify(readingListService, times(1)).findReadingListById(testReadingListId, jwt);
    }

    @Test
    void shouldReturnNullWhenReadingListIsMissing() {
        when(readingListService.findReadingListById(testReadingListId, jwt)).thenReturn(Optional.empty());
        ReadingListResponseDTO result = readingGraphQLController.readingListById(testReadingListId);
        assertNull(result);
    }

    @Test
    void shouldReturnListOfDtosForUser() {
        when(jwt.getSubject()).thenReturn(mockUser.getKeycloakUserId().toString());
        when(readingListService.getReadingListsForUser(mockUser.getKeycloakUserId())).thenReturn(Collections.singletonList(mockReadingList));
        List<ReadingListResponseDTO> result = readingGraphQLController.myReadingLists();
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(expectedReadingListResponseDTO, result.get(0));
        verify(readingListService, times(1)).getReadingListsForUser(mockUser.getKeycloakUserId());
    }

    @Test
    void shouldReturnDtoWhenReadingListIsUpdated() {
        when(readingListService.updateReadingList(testReadingListId, mockReadingListRequestDTO, jwt)).thenReturn(mockReadingList);
        ReadingListResponseDTO result = readingGraphQLController.updateReadingList(testReadingListId, mockReadingListRequestDTO);
        assertEquals(expectedReadingListResponseDTO, result);
        verify(readingListService, times(1)).updateReadingList(testReadingListId, mockReadingListRequestDTO, jwt);
    }

    @Test
    void shouldReturnTrueWhenReadingListIsDeleted() {
        doNothing().when(readingListService).deleteReadingListById(testReadingListId, jwt);
        boolean result = readingGraphQLController.deleteReadingList(testReadingListId);
        assertTrue(result);
        verify(readingListService, times(1)).deleteReadingListById(testReadingListId, jwt);
    }

    @Test
    void shouldReturnTrueWhenBookIsAdded() {
        UUID bookId = UUID.randomUUID();
        doNothing().when(readingListService).addBookToReadingList(testReadingListId, bookId, jwt);
        boolean result = readingGraphQLController.addBookToReadingList(testReadingListId, bookId);
        assertTrue(result);
        verify(readingListService, times(1)).addBookToReadingList(testReadingListId, bookId, jwt);
    }

    @Test
    void shouldReturnTrueWhenBookIsRemoved() {
        UUID bookId = UUID.randomUUID();
        doNothing().when(readingListService).removeBookFromReadingList(testReadingListId, bookId, jwt);
        boolean result = readingGraphQLController.removeBookFromReadingList(testReadingListId, bookId);
        assertTrue(result);
        verify(readingListService, times(1)).removeBookFromReadingList(testReadingListId, bookId, jwt);
    }

    @Test
    void shouldReturnListOfBookDtos() {
        Book mockBook = BookImpl.builder().bookId(UUID.randomUUID()).build();
        when(readingListService.getBooksInReadingList(testReadingListId, jwt)).thenReturn(Collections.singletonList(mockBook));
        List<BookResponseDTO> result = readingGraphQLController.booksInReadingList(testReadingListId);
        assertFalse(result.isEmpty());
        assertEquals(mockBook.getBookId(), result.get(0).getBookId());
        verify(readingListService, times(1)).getBooksInReadingList(testReadingListId, jwt);
    }

    @Test
    void shouldReturnDtoWhenReadingListForBookAndUserIsFound() {
        UUID bookId = UUID.randomUUID();
        when(jwt.getSubject()).thenReturn(mockUser.getKeycloakUserId().toString());
        when(readingListService.findReadingListForBookAndUser(mockUser.getKeycloakUserId(), bookId)).thenReturn(Optional.of(mockReadingList));
        ReadingListResponseDTO result = readingGraphQLController.readingListContainingBook(bookId);
        assertEquals(expectedReadingListResponseDTO, result);
        verify(readingListService, times(1)).findReadingListForBookAndUser(mockUser.getKeycloakUserId(), bookId);
    }

    @Test
    void shouldReturnTrueWhenBookIsMoved() {
        UUID bookId = UUID.randomUUID();
        UUID sourceListId = UUID.randomUUID();
        UUID targetListId = UUID.randomUUID();
        when(jwt.getSubject()).thenReturn(mockUser.getKeycloakUserId().toString());
        doNothing().when(readingListService).moveBookBetweenReadingLists(mockUser.getKeycloakUserId(), bookId, sourceListId, targetListId, jwt);
        boolean result = readingGraphQLController.moveBookBetweenReadingLists(bookId, sourceListId, targetListId);
        assertTrue(result);
        verify(readingListService).moveBookBetweenReadingLists(mockUser.getKeycloakUserId(), bookId, sourceListId, targetListId, jwt);
    }
}