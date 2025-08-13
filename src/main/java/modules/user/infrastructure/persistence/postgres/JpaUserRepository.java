package modules.user.infrastructure.persistence.postgres;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.NotFoundException;
import modules.user.core.domain.User;
import modules.user.core.usecases.repositories.UserRepository;
import modules.user.infrastructure.persistence.postgres.mapper.UserMapper;
import io.quarkus.arc.properties.IfBuildProperty;
import io.quarkus.hibernate.orm.PersistenceUnit;
import org.jboss.logging.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
@IfBuildProperty(name = "app.repository.type", stringValue = "jpa", enableIfMissing = true)
public class JpaUserRepository implements UserRepository {

    private static final Logger LOGGER = Logger.getLogger(JpaUserRepository.class);

    @Inject
    @PersistenceUnit("users-db")
    EntityManager entityManager;

    @Inject
    UserMapper mapper;

    @Override
    public User create(User user) {
        LOGGER.debugf("JPA: Saving or updating user entity with keycloak ID: %s", user.getKeycloakUserId());
        UserEntity userEntity = mapper.toEntity(user);
        UserEntity managedEntity = entityManager.merge(userEntity);
        return mapper.toDomain(managedEntity);
    }

    @Override
    public User update(User user) {
        LOGGER.debugf("JPA: Updating user entity with keycloak ID: %s", user.getKeycloakUserId());
        if (user.getKeycloakUserId() == null) {
            throw new IllegalArgumentException("User ID cannot be null for update");
        }
        UserEntity entity = entityManager.find(UserEntity.class, user.getKeycloakUserId());
        if (entity == null) {
            throw new NotFoundException("User with ID " + user.getKeycloakUserId() + " not found.");
        }
        mapper.updateEntityFromDomain(user, entity);
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<User> findById(UUID userId) {
        LOGGER.debugf("JPA: Finding user entity by ID: %s", userId);
        return Optional.ofNullable(entityManager.find(UserEntity.class, userId))
                .map(mapper::toDomain);
    }

    @Override
    public List<User> findByIds(List<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        TypedQuery<UserEntity> query = entityManager.createQuery(
                "SELECT u FROM UserEntity u WHERE u.keycloakUserId IN :ids", UserEntity.class);
        query.setParameter("ids", userIds);
        return query.getResultList().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findAll() {
        LOGGER.debug("JPA: Finding all user entities");
        return entityManager.createQuery("SELECT u FROM UserEntity u", UserEntity.class)
                .getResultList()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID userId) {
        LOGGER.debugf("JPA: Deleting user entity with ID: %s", userId);
        UserEntity entity = entityManager.find(UserEntity.class, userId);
        if (entity != null) {
            entityManager.remove(entity);
        }
    }
}