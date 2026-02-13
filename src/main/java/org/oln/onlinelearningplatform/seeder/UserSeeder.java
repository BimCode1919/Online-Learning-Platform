package org.oln.onlinelearningplatform.seeder;

import org.oln.onlinelearningplatform.entity.User;
import org.oln.onlinelearningplatform.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserSeeder {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void seed() {
        if (userRepository.count() == 0) {
            // 1. Tạo tài khoản Admin
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123"));
            admin.setEmail("admin@gmail.com");
            admin.setRole("ADMIN");
            userRepository.save(admin);

            // 2. Tạo tài khoản Instructor (Giảng viên)
            User instructor = new User();
            instructor.setUsername("teacher");
            instructor.setPassword(passwordEncoder.encode("123"));
            instructor.setEmail("teacher@gmail.com");
            instructor.setRole("INSTRUCTOR");
            userRepository.save(instructor);

            // 3. Tạo tài khoản Student (Học viên)
            User student = new User();
            student.setUsername("student");
            student.setPassword(passwordEncoder.encode("123"));
            student.setEmail("student@gmail.com");
            student.setRole("STUDENT");
            userRepository.save(student);

            System.out.println(">>> SEEDER: Đã khởi tạo 3 Users (admin, teacher, student).");
        }
    }
}
