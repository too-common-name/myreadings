package org.modular.playground.readinglist.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modular.playground.readinglist.core.domain.ReadingList;
import org.modular.playground.readinglist.core.usecases.ReadingListService;
import org.modular.playground.readinglist.infrastructure.messaging.KeycloakUserEventListenerCreateReadingLists;
import org.modular.playground.user.core.domain.User;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class KeycloakUserEventListenerCreateReadingListsTest {

    @InjectMocks
    KeycloakUserEventListenerCreateReadingLists listener;

    @Mock
    ReadingListService readingListService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateDefaultListsWhenUserObjectIsReceived() {
        User mockUser = mock(User.class);
        when(mockUser.getKeycloakUserId()).thenReturn(UUID.randomUUID());

        ArgumentCaptor<ReadingList> readingListCaptor = ArgumentCaptor.forClass(ReadingList.class);

        listener.processUserCreation(mockUser);

        verify(readingListService, times(2)).createReadingListInternal(readingListCaptor.capture());

        List<ReadingList> capturedLists = readingListCaptor.getAllValues();
        assertEquals(2, capturedLists.size());
        assertTrue(capturedLists.stream().anyMatch(list -> list.getName().equals("To Read")));
        assertTrue(capturedLists.stream().anyMatch(list -> list.getName().equals("Read")));
    }

    @Test
    void shouldHandleServiceExceptionsGracefully() {
        User mockUser = mock(User.class);

        doThrow(new RuntimeException("Database connection failed")).when(readingListService).createReadingListInternal(any());

        assertDoesNotThrow(() -> {
            listener.processUserCreation(mockUser);
        });
    }
}