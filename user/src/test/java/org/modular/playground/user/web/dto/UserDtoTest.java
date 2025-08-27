package org.modular.playground.user.web.dto;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class UserDtoTest {

    @Test
    void testUserResponseDTO() {
        UUID userId = UUID.randomUUID();
        UserResponseDTO dto1 = UserResponseDTO.builder().userId(userId).username("test").build();
        UserResponseDTO dto2 = UserResponseDTO.builder().userId(userId).username("test").build();

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertEquals("test", dto1.getUsername());
        assertTrue(dto1.toString().contains("test"));
    }
}