package org.oln.onlinelearningplatform.service.user;

import org.oln.onlinelearningplatform.entity.User;

import java.util.Optional;

public interface UserService {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
