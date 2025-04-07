package modules.user.core.usecases.repositories;

import java.util.UUID;

import modules.user.core.domain.User;

import java.util.Optional;
import java.util.List;

public interface UserRepository {

    User save(User user); 
    Optional<User> findById(UUID userId);
    List<User> findAll();
    void deleteById(UUID userId);

}