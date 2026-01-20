package org.oln.onlinelearningplatform.service.user.impl;

import org.oln.onlinelearningplatform.entity.User;
import org.oln.onlinelearningplatform.repository.UserRepository;
import org.oln.onlinelearningplatform.service.user.UserService;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent(); // Giả sử repo chưa có existsBy
    }
}