package modules.user.infrastructure.persistence.postgres;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import modules.user.core.domain.User;
import modules.user.core.usecases.repositories.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import io.quarkus.arc.properties.IfBuildProperty;
import io.quarkus.hibernate.orm.PersistenceUnit;

@ApplicationScoped
@IfBuildProperty(name = "app.repository.type", stringValue = "jpa")
public class JpaUserRepository implements UserRepository {

    @Inject
    @PersistenceUnit("users-db")
    EntityManager entityManager;

    @Inject
    UserPersistenceMapper mapper;

    @Override
    public User save(User user) {
        UserEntity userEntity = mapper.toEntity(user);
        entityManager.persist(userEntity);
        return mapper.toDomain(userEntity);
    }

    @Override
    public Optional<User> findById(UUID userId) {
        return Optional.ofNullable(entityManager.find(UserEntity.class, userId))
                .map(mapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        return entityManager.createQuery("SELECT u FROM UserEntity u", UserEntity.class)
                .getResultList()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID userId) {
        Optional.ofNullable(entityManager.find(UserEntity.class, userId))
                .ifPresent(entityManager::remove);
    }

}