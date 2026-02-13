package org.oln.onlinelearningplatform.service.auth.impl;

import jakarta.transaction.Transactional;
import org.oln.onlinelearningplatform.entity.User;
import org.oln.onlinelearningplatform.repository.UserRepository;
import org.oln.onlinelearningplatform.service.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void registerUser(User user, String role) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_" + role); // Set role dựa trên tham số truyền vào
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}
