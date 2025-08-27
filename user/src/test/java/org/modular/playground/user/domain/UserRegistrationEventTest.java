package org.modular.playground.user.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.modular.playground.user.core.domain.UserRegistrationEvent;

public class UserRegistrationEventTest {
    @Test
    void testUserRegistrationEvent() {
        UserRegistrationEvent event1 = new UserRegistrationEvent();
        event1.setUsername("test");
        event1.setEmail("test@test.com");

        UserRegistrationEvent event2 = new UserRegistrationEvent();
        event2.setUsername("test");
        event2.setEmail("test@test.com");

        assertEquals(event1, event2);
        assertEquals(event1.hashCode(), event2.hashCode());
        assertTrue(event1.toString().contains("test"));
    }
}
