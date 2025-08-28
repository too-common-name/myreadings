package org.modular.playground.user.infrastructure.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.modular.playground.user.core.domain.UiTheme;
import org.modular.playground.user.core.domain.User;
import org.modular.playground.user.core.domain.UserImpl;
import org.modular.playground.user.infrastructure.messaging.KeycloakEventDTO;
import org.modular.playground.user.infrastructure.persistence.postgres.UserEntity;
import org.modular.playground.user.infrastructure.persistence.postgres.mapper.UserMapper;
import org.modular.playground.user.infrastructure.persistence.postgres.mapper.UserMapperImpl;
import org.modular.playground.user.utils.UserTestUtils;
import org.modular.playground.user.web.dto.UserResponseDTO;

public class UserMapperTest {
    private final UserMapper userMapper = new UserMapperImpl();

    @Test
    void shouldMapEntityToDomain() {
        UserEntity entity = new UserEntity();
        entity.setKeycloakUserId(UUID.randomUUID());
        entity.setUsername("testuser");
        entity.setFirstName("Test");
        entity.setLastName("User");
        entity.setEmail("test@example.com");
        entity.setThemePreference(UiTheme.DARK);

        User domainUser = userMapper.toDomain(entity);

        assertNotNull(domainUser);
        assertEquals(entity.getKeycloakUserId(), domainUser.getKeycloakUserId());
        assertEquals(entity.getUsername(), domainUser.getUsername());
        assertEquals(entity.getFirstName(), domainUser.getFirstName());
        assertEquals(entity.getLastName(), domainUser.getLastName());
        assertEquals(entity.getEmail(), domainUser.getEmail());
        assertEquals(UiTheme.DARK, domainUser.getThemePreference());
    }

    @Test
    void shouldMapDomainToEntity() {
        User domainUser = UserTestUtils.createValidUser();

        UserEntity entity = userMapper.toEntity(domainUser);

        assertNotNull(entity);
        assertEquals(domainUser.getKeycloakUserId(), entity.getKeycloakUserId());
        assertEquals(domainUser.getUsername(), entity.getUsername());
    }

    @Test
    void shouldMapDomainToResponseDTO() {
        User domainUser = UserTestUtils.createValidUser();

        UserResponseDTO dto = userMapper.toResponseDTO(domainUser);

        assertNotNull(dto);
        assertEquals(domainUser.getKeycloakUserId(), dto.getUserId());
        assertEquals(domainUser.getUsername(), dto.getUsername());
    }

    @Test
    void shouldMapKeycloakEventToDomain() {
        KeycloakEventDTO.Details details = new KeycloakEventDTO.Details();
        details.setUsername("keycloak_user");
        details.setEmail("keycloak@example.com");
        details.setFirstName("Keycloak");
        details.setLastName("User");

        KeycloakEventDTO event = new KeycloakEventDTO();
        event.setUserId(UUID.randomUUID().toString());
        event.setDetails(details);

        User domainUser = userMapper.toDomain(event);

        assertNotNull(domainUser);
        assertEquals(UUID.fromString(event.getUserId()), domainUser.getKeycloakUserId());
        assertEquals(details.getUsername(), domainUser.getUsername());
        assertEquals(details.getEmail(), domainUser.getEmail());
        assertEquals(details.getFirstName(), domainUser.getFirstName());
        assertEquals(details.getLastName(), domainUser.getLastName());
    }

    @Test
    void shouldUpdateEntityFromDomain() {
        UserEntity existingEntity = new UserEntity();
        existingEntity.setKeycloakUserId(UserTestUtils.createValidUser().getKeycloakUserId());
        existingEntity.setFirstName("OldName");
        existingEntity.setThemePreference(UiTheme.LIGHT);
        
        User domainWithUpdates = UserImpl.builder()
                .keycloakUserId(existingEntity.getKeycloakUserId())
                .firstName("NewName")
                .lastName("NewLastName")
                .username("new_username")
                .email("new@email.com")
                .themePreference(UiTheme.DARK)
                .build();

        userMapper.updateEntityFromDomain(domainWithUpdates, existingEntity);

        assertEquals("NewName", existingEntity.getFirstName());
        assertEquals("NewLastName", existingEntity.getLastName());
        assertEquals(UiTheme.DARK, existingEntity.getThemePreference());
        assertEquals(domainWithUpdates.getKeycloakUserId(), existingEntity.getKeycloakUserId());
    }
}
