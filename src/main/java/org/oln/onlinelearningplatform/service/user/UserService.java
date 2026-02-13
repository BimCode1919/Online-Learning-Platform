package org.oln.onlinelearningplatform.service.user;

import org.oln.onlinelearningplatform.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    User findByEmail(String email);
    List<User> findAllUsers(); // Lấy tất cả
    void deleteUserById(Long id); // Xóa theo ID
    void saveUser(User user); // Lưu user (dùng cho cả update/create)
    Optional<User> findById(Long id); // Tìm theo ID để edit
}
