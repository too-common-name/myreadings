package modules.readinglist.infrastructure.messaging;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import modules.readinglist.core.domain.ReadingList;
import modules.readinglist.core.domain.ReadingListImpl;
import modules.readinglist.core.usecases.ReadingListService;
import modules.user.infrastructure.messaging.KeycloakUserEventListener;
import jakarta.json.JsonObject;
import java.util.UUID;

public class KeycloakUserEventListenerCreateReadingLists {

    private static final Logger log = LoggerFactory.getLogger(KeycloakUserEventListener.class);

    @Inject
    ReadingListService readingListService;

    @Incoming("registrations")
    public void processUserEvent(byte[] event) {
        String message = new String(event, StandardCharsets.UTF_8);
        log.info("Received raw user registration event: %s", message);

        try (Jsonb jsonb = JsonbBuilder.create()) {
            JsonObject root = jsonb.fromJson(message, JsonObject.class);
            String userIdString = root.getString("userId", null);

            if (userIdString != null) {
                UUID userId = UUID.fromString(userIdString);
                createDefaultReadingListsForUser(userId);
            } else {
                log.warn("Could not find 'userId' in the event payload.");
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format for userId in event: %s", message, e);
        } catch (Exception e) {
            log.error("Failed to process user registration event.", e);
        }
    }

    private void createDefaultReadingListsForUser(UUID userId) {
        ReadingList toReadList = ReadingListImpl.builder()
                .userId(userId)
                .name("To Read")
                .creationDate(LocalDateTime.now())
                .description("Books I plan to read.")
                .build();

        ReadingList alreadyReadList = ReadingListImpl.builder()
                .userId(userId)
                .name("Read")
                .creationDate(LocalDateTime.now())
                .description("Books I have already completed.")
                .build();

        readingListService.createReadingList(toReadList);
        log.info("Created '%s' list for user %s", toReadList.getName(), userId);

        readingListService.createReadingList(alreadyReadList);
        log.info("Created '%s' list for user %s", alreadyReadList.getName(), userId);
    }
}
