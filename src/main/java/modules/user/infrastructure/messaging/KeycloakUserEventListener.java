package modules.user.infrastructure.messaging;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import modules.user.core.domain.User;
import modules.user.core.domain.UserImpl;
import modules.user.core.domain.UserRegistrationEvent;
import modules.user.core.usecases.UserService;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@ApplicationScoped
public class KeycloakUserEventListener {

    private static final Logger log = LoggerFactory.getLogger(KeycloakUserEventListener.class);

    @Inject
    UserService userService;

    @Incoming("registrations")
    public void processUserEvent(byte[] event) {
        String message = new String(event, StandardCharsets.UTF_8);
        log.info("Received Keycloak user event (byte): {}", message);
        try (Jsonb jsonb = JsonbBuilder.create()) {
            JsonObject root = jsonb.fromJson(message, JsonObject.class);
            JsonObject details = root.getJsonObject("details");
            if (details != null) {
                UserRegistrationEvent registrationEvent = new UserRegistrationEvent();
                registrationEvent.setUsername(details.getString("username"));
                registrationEvent.setEmail(details.getString("email"));
                registrationEvent.setFirstName(details.getString("first_name"));
                registrationEvent.setLastName(details.getString("last_name"));

                log.info("Parsed User Registration Event: {}", registrationEvent);

                
                UUID keycloakUserId = UUID.fromString(root.getString("userId"));

                User user = UserImpl.builder()
                        .keycloakUserId(keycloakUserId)
                        .firstName(registrationEvent.getFirstName())
                        .lastName(registrationEvent.getLastName())
                        .username(registrationEvent.getUsername())
                        .email(registrationEvent.getEmail())
                        .build();

                
                userService.createUserProfile(user);

                log.info("User registered: {}", user.getUsername());

            } else {
                log.warn("Details not found in the event: {}", message);
            }
        } catch (Exception e) {
            log.error("Error parsing JSON or creating user: {}", e.getMessage(), e);
        }
    }
}