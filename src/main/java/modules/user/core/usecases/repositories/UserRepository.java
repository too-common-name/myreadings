package modules.user.core.usecases.repositories;

import java.util.UUID;

import modules.user.core.domain.User;

import java.util.Optional;
import java.util.List;

public interface UserRepository {

    User create(User user); 
    User update(User user);
    Optional<User> findById(UUID userId);
    List<User> findByIds(List<UUID> userIds);
    List<User> findAll();
    void deleteById(UUID userId);

}