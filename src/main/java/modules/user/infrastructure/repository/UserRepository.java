package modules.user.infrastructure.repository;

import modules.user.domain.User;
import java.util.UUID;
import java.util.Optional;
import java.util.List;

public interface UserRepository {

    User save(User user); 
    Optional<User> findById(UUID userId);
    Optional<User> findByUsername(String username);
    List<User> findAll();
    void deleteById(UUID userId);

}