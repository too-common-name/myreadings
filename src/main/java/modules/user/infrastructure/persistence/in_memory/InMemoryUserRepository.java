package modules.user.infrastructure.persistence.in_memory;

import jakarta.enterprise.context.ApplicationScoped;
import modules.user.core.domain.User;
import modules.user.core.usecases.repositories.UserRepository;
import io.quarkus.arc.properties.IfBuildProperty;
import org.jboss.logging.Logger;

import java.util.*;

@ApplicationScoped
@IfBuildProperty(name = "app.repository.type", stringValue = "in-memory", enableIfMissing = true)
public class InMemoryUserRepository implements UserRepository {

    private static final Logger LOGGER = Logger.getLogger(InMemoryUserRepository.class);
    private final Map<UUID, User> users = new HashMap<>();

    @Override
    public User save(User user) {
        LOGGER.debugf("In-memory: Saving or updating user with keycloak ID: %s", user.getKeycloakUserId());
        users.put(user.getKeycloakUserId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(UUID userId) {
        LOGGER.debugf("In-memory: Finding user by ID: %s", userId);
        return Optional.ofNullable(users.get(userId));
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