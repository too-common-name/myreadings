package modules.user.infrastructure.persistence.in_memory;

import jakarta.enterprise.context.ApplicationScoped;
import modules.user.core.domain.User;
import modules.user.core.usecases.repositories.UserRepository;

import java.util.UUID;

import io.quarkus.arc.properties.IfBuildProperty;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
@IfBuildProperty(name = "app.repository.type", stringValue = "in-memory", enableIfMissing = true)
public class InMemoryUserRepository implements UserRepository {

    private final Map<UUID, User> users = new HashMap<>();

    @Override
    public User save(User user) {
        users.put(user.getKeycloakUserId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(UUID userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void deleteById(UUID userId) {
        users.remove(userId);
    }
}