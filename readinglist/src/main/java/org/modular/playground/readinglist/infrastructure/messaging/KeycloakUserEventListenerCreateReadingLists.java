package org.modular.playground.readinglist.infrastructure.messaging;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

import org.modular.playground.common.filters.TraceIdFilter;
import org.modular.playground.readinglist.core.domain.ReadingList;
import org.modular.playground.readinglist.core.domain.ReadingListImpl;
import org.modular.playground.readinglist.core.usecases.ReadingListService;
import org.modular.playground.user.core.domain.User;
import org.modular.playground.user.core.usecases.UserService;
import org.modular.playground.user.infrastructure.messaging.KeycloakEventDTO;

@ApplicationScoped
public class KeycloakUserEventListenerCreateReadingLists {

    private static final Logger LOGGER = Logger.getLogger(KeycloakUserEventListenerCreateReadingLists.class);

    @Inject
    ReadingListService readingListService;

    @Inject
    UserService userService;

    @Incoming("registrations")
    public void processUserEvent(byte[] event) {
        MDC.put(TraceIdFilter.TRACE_ID_KEY, "event-" + UUID.randomUUID().toString());
        String message = new String(event, StandardCharsets.UTF_8);
        LOGGER.infof("Received user registration event to create default reading lists");
        LOGGER.debugf("Event payload: %s", message);

        try (Jsonb jsonb = JsonbBuilder.create()) {
            KeycloakEventDTO keycloakEvent = jsonb.fromJson(message, KeycloakEventDTO.class);
            String userIdString = keycloakEvent.getUserId();

            if (userIdString != null) {
                UUID userId = UUID.fromString(userIdString);
                userService.findUserProfileById(userId, null)
                    .ifPresentOrElse(
                        this::createDefaultReadingListsForUser,
                        () -> LOGGER.warnf("User with ID %s not found, cannot create default lists.", userId)
                    );
            } else {
                LOGGER.warn("Could not find 'userId' in the event payload.");
            }
        } catch (Exception e) {
            LOGGER.errorf(e, "Failed to process user registration event. Payload: %s", message);
        } finally {
            MDC.remove(TraceIdFilter.TRACE_ID_KEY);
        }
    }

    private void createDefaultReadingListsForUser(User user) {
        ReadingList toReadList = ReadingListImpl.builder()
                .readingListId(UUID.randomUUID())
                .user(user)
                .name("To Read")
                .creationDate(LocalDateTime.now())
                .description("Books I plan to read.")
                .build();

        ReadingList alreadyReadList = ReadingListImpl.builder()
                .readingListId(UUID.randomUUID())
                .user(user)
                .name("Read")
                .creationDate(LocalDateTime.now())
                .description("Books I have already completed.")
                .build();

        readingListService.createReadingListInternal(toReadList);
        readingListService.createReadingListInternal(alreadyReadList);
    }
}