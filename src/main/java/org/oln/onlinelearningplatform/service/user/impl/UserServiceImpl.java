package org.oln.onlinelearningplatform.service.user.impl;

import org.oln.onlinelearningplatform.entity.User;
import org.oln.onlinelearningplatform.repository.QuizAttemptRepository;
import org.oln.onlinelearningplatform.repository.UserProgressRepository;
import org.oln.onlinelearningplatform.repository.UserRepository;
import org.oln.onlinelearningplatform.service.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    // --- THÊM 2 CÁI NÀY ---
    private final UserRepository userRepository;
    private final UserProgressRepository userProgressRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    // --- CẬP NHẬT CONSTRUCTOR ĐỂ INJECT VÀO ---
    public UserServiceImpl(UserRepository userRepository,
                           UserProgressRepository userProgressRepository,
                           QuizAttemptRepository quizAttemptRepository) {
        this.userRepository = userRepository;
        this.userProgressRepository = userProgressRepository;
        this.quizAttemptRepository = quizAttemptRepository;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }


    @Override
    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent(); // Giả sử repo chưa có existsBy
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }



    @Override
    public void saveUser(User user) {
        userRepository.save(user); // Hàm save của JPA tự động xử lý (có ID thì update, ko có thì insert)
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional // Quan trọng: Để đảm bảo xóa hết hoặc không xóa gì
    public void deleteUserById(Long id) {
        // 1. Xóa tiến độ học tập trước
        userProgressRepository.deleteByUserId(id);

        // 2. Xóa kết quả thi (nếu có)
        quizAttemptRepository.deleteByUserId(id);

        // 3. Cuối cùng mới xóa User
        userRepository.deleteById(id);
    }



}