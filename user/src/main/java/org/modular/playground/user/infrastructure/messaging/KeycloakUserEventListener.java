package org.modular.playground.user.infrastructure.messaging;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

import org.modular.playground.common.filters.TraceIdFilter;
import org.modular.playground.user.core.domain.User;
import org.modular.playground.user.core.usecases.UserService;
import org.modular.playground.user.infrastructure.persistence.postgres.mapper.UserMapper;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@ApplicationScoped
public class KeycloakUserEventListener {

    private static final Logger LOGGER = Logger.getLogger(KeycloakUserEventListener.class);

    @Inject
    UserService userService;

    @Inject
    UserMapper userMapper;

    @Incoming("registrations")
    public void processUserEvent(byte[] event) {
        MDC.put(TraceIdFilter.TRACE_ID_KEY, "event-" + UUID.randomUUID().toString());
        String message = new String(event, StandardCharsets.UTF_8);
        LOGGER.infof("Received Keycloak user event: %s", message);

        try (Jsonb jsonb = JsonbBuilder.create()) {
            KeycloakEventDTO registrationEvent = jsonb.fromJson(message, KeycloakEventDTO.class);

            if (registrationEvent != null && registrationEvent.getDetails() != null) {
                LOGGER.debugf("Successfully parsed user registration event for username: %s",
                        registrationEvent.getDetails().getUsername());
                
                User user = userMapper.toDomain(registrationEvent);
                
                userService.createUserProfile(user);

            } else {
                LOGGER.warnf("Keycloak event details not found or failed to parse. Message: %s", message);
            }
        } catch (Exception e) {
            LOGGER.errorf(e, "Error processing Keycloak event. Message: %s", message);
        } finally {
            MDC.remove(TraceIdFilter.TRACE_ID_KEY);
        }
    }
}