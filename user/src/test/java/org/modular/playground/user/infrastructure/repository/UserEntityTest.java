package org.modular.playground.user.infrastructure.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.modular.playground.user.infrastructure.persistence.postgres.UserEntity;

public class UserEntityTest {
    @Test
    void testEqualsContract() {
        UUID userId = UUID.randomUUID();

        UserEntity entity1 = UserEntity.builder()
                .keycloakUserId(userId)
                .username("testuser")
                .build();

        UserEntity entity2 = UserEntity.builder()
                .keycloakUserId(userId)
                .username("testuser")
                .build();

        UserEntity entity3 = UserEntity.builder()
                .keycloakUserId(UUID.randomUUID())
                .username("anotheruser")
                .build();

        assertEquals(entity1, entity2);
        assertNotEquals(entity1, entity3);
        assertNotEquals(entity1, null);
        assertNotEquals(entity1, new Object());
    }

    @Test
    void testHashCodeContract() {
        UUID userId = UUID.randomUUID();

        UserEntity entity1 = UserEntity.builder()
                .keycloakUserId(userId)
                .username("testuser")
                .build();

        UserEntity entity2 = UserEntity.builder()
                .keycloakUserId(userId)
                .username("testuser")
                .build();
        
        UserEntity entity3 = UserEntity.builder()
                .keycloakUserId(UUID.randomUUID())
                .username("anotheruser")
                .build();

        assertEquals(entity1.hashCode(), entity2.hashCode());
        assertNotEquals(entity1.hashCode(), entity3.hashCode());
    }

    @Test
    void testToString() {
        UserEntity entity = UserEntity.builder()
                .keycloakUserId(UUID.randomUUID())
                .username("testuser")
                .build();

        assertTrue(entity.toString().contains("testuser"));
    }
}
