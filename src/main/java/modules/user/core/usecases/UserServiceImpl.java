package modules.user.core.usecases;

import modules.user.core.domain.User;
import modules.user.core.usecases.repositories.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.UUID;
import java.util.Optional;

@ApplicationScoped
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Inject
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User createUserProfile(User user) {
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findUserProfileById(UUID userId) {
        return userRepository.findById(userId);
    }

    @Override
    public User updateUserProfile(User user) {
        return userRepository.save(user);
    }

    @Override
    public void deleteUserProfile(UUID userId) {
        userRepository.deleteById(userId);
    }
}