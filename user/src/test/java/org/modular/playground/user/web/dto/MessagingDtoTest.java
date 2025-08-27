package org.modular.playground.user.web.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.modular.playground.user.infrastructure.messaging.KeycloakEventDTO;

public class MessagingDtoTest {
    @Test
    void testKeycloakEventDTO() {
        KeycloakEventDTO.Details details1 = new KeycloakEventDTO.Details();
        details1.setUsername("details_user");
        KeycloakEventDTO event1 = new KeycloakEventDTO();
        event1.setUserId("user123");
        event1.setDetails(details1);
        
        KeycloakEventDTO.Details details2 = new KeycloakEventDTO.Details();
        details2.setUsername("details_user");
        KeycloakEventDTO event2 = new KeycloakEventDTO();
        event2.setUserId("user123");
        event2.setDetails(details2);

        assertEquals(event1, event2);
        assertEquals(event1.hashCode(), event2.hashCode());
        assertTrue(event1.toString().contains("user123"));
    }
}
