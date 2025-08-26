package org.modular.playground.user.infrastructure.persistence.in_memory;

import jakarta.enterprise.context.ApplicationScoped;
import org.modular.playground.user.core.domain.User;
import org.modular.playground.user.core.usecases.repositories.UserRepository;

import io.quarkus.arc.properties.IfBuildProperty;

import org.jboss.logging.Logger;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
@IfBuildProperty(name = "app.repository.type", stringValue = "in-memory", enableIfMissing = true)
public class InMemoryUserRepository implements UserRepository {

    private static final Logger LOGGER = Logger.getLogger(InMemoryUserRepository.class);
    private final Map<UUID, User> users = new HashMap<>();

    @Override
    public User create(User user) {
        LOGGER.debugf("In-memory: Saving or updating user with keycloak ID: %s", user.getKeycloakUserId());
        users.put(user.getKeycloakUserId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        LOGGER.debugf("In-memory: Updating user with keycloak ID: %s", user.getKeycloakUserId());
        if (user.getKeycloakUserId() == null || !users.containsKey(user.getKeycloakUserId())) {
            throw new IllegalArgumentException("User with ID " + user.getKeycloakUserId() + " not found for update.");
        }
        users.put(user.getKeycloakUserId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(UUID userId) {
        LOGGER.debugf("In-memory: Finding user by ID: %s", userId);
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public List<User> findByIds(List<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        return userIds.stream()
                .map(users::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findAll() {
        LOGGER.debug("In-memory: Finding all users");
        return new ArrayList<>(users.values());
    }

    @Override
    public void deleteById(UUID userId) {
        LOGGER.debugf("In-memory: Deleting user with ID: %s", userId);
        users.remove(userId);
    }
}