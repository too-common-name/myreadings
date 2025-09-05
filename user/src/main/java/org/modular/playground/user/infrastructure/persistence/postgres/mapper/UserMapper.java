package org.modular.playground.user.infrastructure.persistence.postgres.mapper;

import org.modular.playground.user.core.domain.User;
import org.modular.playground.user.core.domain.UserImpl;
import org.modular.playground.user.infrastructure.messaging.KeycloakEventDTO;
import org.modular.playground.user.infrastructure.persistence.postgres.UserEntity;
import org.modular.playground.user.web.dto.UserResponseDTO;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import java.util.UUID;

@Mapper(componentModel = "cdi", imports = UUID.class)
public interface UserMapper {

    @Mapping(target = "keycloakUserId", expression = "java(UUID.fromString(dto.getUserId()))")
    @Mapping(target = "themePreference", constant = "LIGHT")
    @Mapping(source = "details.firstName", target = "firstName")
    @Mapping(source = "details.lastName", target = "lastName")
    @Mapping(source = "details.email", target = "email")
    @Mapping(source = "details.username", target = "username")
    UserImpl toDomain(KeycloakEventDTO dto);

    @Mapping(source = "keycloakUserId", target = "userId")
    UserResponseDTO toResponseDTO(User user);

    UserEntity toEntity(User user);

    UserImpl toDomain(UserEntity entity);

    void updateEntityFromDomain(User user, @MappingTarget UserEntity entity);
}