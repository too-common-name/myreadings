package org.modular.playground.readinglist.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modular.playground.readinglist.core.usecases.ReadingListService;
import org.modular.playground.readinglist.infrastructure.messaging.KeycloakUserEventListenerCreateReadingLists;
import org.modular.playground.user.core.domain.User;
import org.modular.playground.user.core.usecases.UserService;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class KeycloakUserEventListenerCreateReadingListsTest {

    @InjectMocks
    KeycloakUserEventListenerCreateReadingLists listener;

    @Mock
    ReadingListService readingListService;

    @Mock
    UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateInitialReadingListsOnValidEvent() {
        String validEventJson = "{\n" +
                "  \"@class\" : \"com.github.aznamier.keycloak.event.provider.EventClientNotificationMqMsg\",\n" +
                "  \"time\" : 1742495768687,\n" +
                "  \"type\" : \"REGISTER\",\n" +
                "  \"realmId\" : \"myreadings\",\n" +
                "  \"clientId\" : \"myreadings-app\",\n" +
                "  \"userId\" : \"a9a2e247-f06f-4023-82e5-a0a07cee5c63\",\n" +
                "  \"ipAddress\" : \"10.89.1.7\",\n" +
                "  \"details\" : {\n" +
                "    \"auth_method\" : \"openid-connect\",\n" +
                "    \"auth_type\" : \"code\",\n" +
                "    \"register_method\" : \"form\",\n" +
                "    \"last_name\" : \"Rossi\",\n" +
                "    \"redirect_uri\" : \"http://localhost:3000/\",\n" +
                "    \"first_name\" : \"Daniele\",\n" +
                "    \"code_id\" : \"d4aa538e-5025-4ac2-a149-dbeff00090e1\",\n" +
                "    \"email\" : \"drossi@redhat.com\",\n" +
                "    \"username\" : \"drossi\"\n" +
                "  }\n" +
                "}";
        User mockUser = mock(User.class);
        when(userService.findUserByIdInternal(any(UUID.class))).thenReturn(Optional.of(mockUser));

        listener.processUserEvent(validEventJson.getBytes());

        verify(readingListService, times(2)).createReadingListInternal(any());
    }

    @Test
    void shouldNotCreateListsWhenEventIsMissingDetails() {
        String eventWithoutDetails = "{\"userId\":\"some-uuid\",\"type\":\"REGISTER\"}";

        listener.processUserEvent(eventWithoutDetails.getBytes(StandardCharsets.UTF_8));

        verify(readingListService, never()).createReadingListInternal(any());
    }

    @Test
    void shouldNotCrashWhenEventIsCorrupt() {
        String corruptEvent = "this is not json";

        assertDoesNotThrow(() -> {
            listener.processUserEvent(corruptEvent.getBytes(StandardCharsets.UTF_8));
        });

        verify(readingListService, never()).createReadingListInternal(any());
    }
    
}
