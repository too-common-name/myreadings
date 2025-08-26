package org.modular.playground.user.utils;

import java.util.List;
import java.util.UUID;

import org.modular.playground.user.core.domain.User;
import org.modular.playground.user.core.usecases.repositories.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class UserRepositoryUtils {
    
    @Inject
    UserRepository userRepository;

    @Transactional
    public User saveUser(User user) {
        return userRepository.create(user);
    }

    @Transactional
    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

}
