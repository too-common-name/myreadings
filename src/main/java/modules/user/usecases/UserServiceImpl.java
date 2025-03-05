package modules.user.usecases;

import modules.user.domain.User;
import modules.user.domain.UserImpl;
import modules.user.infrastructure.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;
import java.util.Optional;

@ApplicationScoped
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Inject
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User createUserProfile(UserImpl.UserBuilder userBuilder) {
        User user = userBuilder.build();
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