package org.modular.playground.readinglist.infrastructure.messaging;

import java.time.LocalDateTime;
import java.util.UUID;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.modular.playground.common.filters.TraceIdFilter;
import org.modular.playground.readinglist.core.domain.ReadingList;
import org.modular.playground.readinglist.core.domain.ReadingListImpl;
import org.modular.playground.readinglist.core.usecases.ReadingListService;
import org.modular.playground.user.core.domain.User;
import org.modular.playground.user.core.usecases.UserService;

import io.smallrye.common.annotation.Blocking;

@ApplicationScoped
public class KeycloakUserEventListenerCreateReadingLists {

    private static final Logger LOGGER = Logger.getLogger(KeycloakUserEventListenerCreateReadingLists.class);

    @Inject
    ReadingListService readingListService;

    @Inject
    UserService userService;

    @Incoming("user-profile-created")
    @Blocking
    public void processUserCreation(User user) {
        MDC.put(TraceIdFilter.TRACE_ID_KEY, "event-" + UUID.randomUUID().toString());
        LOGGER.infof("Received internal user profile created event for user: %s", user.getUsername());
        
        try {
            createDefaultReadingListsForUser(user);
            LOGGER.infof("Successfully created default reading lists for user %s.", user.getKeycloakUserId());
        } catch (Exception e) {
            LOGGER.errorf(e, "Failed to create default reading lists for user %s", user.getKeycloakUserId());
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