package org.modular.playground.user.infrastructure.persistence.postgres.mapper;

import org.modular.playground.user.core.domain.User;
import org.modular.playground.user.web.dto.UserResponseDTO;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface UserMapper {

    @Mapping(source = "keycloakUserId", target = "userId")
    UserResponseDTO toResponseDTO(User user);
}
