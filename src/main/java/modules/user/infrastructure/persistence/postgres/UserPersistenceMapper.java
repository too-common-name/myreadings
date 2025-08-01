package modules.user.infrastructure.persistence.postgres;

import jakarta.enterprise.context.ApplicationScoped;
import modules.user.core.domain.User;
import modules.user.core.domain.UserImpl;

@ApplicationScoped
public class UserPersistenceMapper {

    public UserEntity toEntity(User user) {
        return UserEntity.builder()
                .keycloakUserId(user.getKeycloakUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .email(user.getEmail())
                .themePreference(user.getThemePreference())
                .build();
    }

    public User toDomain(UserEntity userEntity) {
        return UserImpl.builder()
                .keycloakUserId(userEntity.getKeycloakUserId())
                .firstName(userEntity.getFirstName())
                .lastName(userEntity.getLastName())
                .username(userEntity.getUsername())
                .email(userEntity.getEmail())
                .themePreference(userEntity.getThemePreference())
                .build();
    }
}