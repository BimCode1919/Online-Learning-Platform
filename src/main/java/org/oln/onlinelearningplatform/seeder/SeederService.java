package org.oln.onlinelearningplatform.seeder;

import lombok.RequiredArgsConstructor;
import org.oln.onlinelearningplatform.entity.Course;
import org.oln.onlinelearningplatform.entity.Lesson;
import org.oln.onlinelearningplatform.entity.User;
import org.oln.onlinelearningplatform.repository.CourseRepository;
import org.oln.onlinelearningplatform.repository.LessonRepository;
import org.oln.onlinelearningplatform.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeederService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void seedAllData() {
        // 1. Tạo Users
        User admin = createUser("admin", "admin@olp.com", "123", "ADMIN");
        User instructor = createUser("teacher", "teacher@olp.com", "123", "INSTRUCTOR");
        User student = createUser("student", "student@olp.com", "123", "STUDENT");
        userRepository.saveAll(List.of(admin, instructor, student));

        // Danh sách các chủ đề khóa học để tạo 10 courses
        String[] topics = {
                "Java Web", "Python Data Science", "ReactJS Basic", "C# .NET Core",
                "AWS Cloud", "Node.js Backend", "Mobile App Flutter", "UI/UX Design",
                "Docker & Kubernetes", "Machine Learning"
        };

        for (int i = 0; i < topics.length; i++) {
            // 2. Tạo Course
            Course course = new Course();
            course.setTitle("Khóa học " + topics[i]);
            course.setDescription("Mô tả chi tiết cho " + topics[i] + ". Khóa học này cung cấp kiến thức thực chiến.");
            course.setInstructor(instructor);
            Course savedCourse = courseRepository.save(course);

            // 3. Tạo 3 Lessons cho mỗi Course
            List<Lesson> lessons = new ArrayList<>();
            for (int j = 1; j <= 3; j++) {
                Lesson lesson = new Lesson();
                lesson.setTitle("Bài " + j + ": Nội dung " + topics[i] + " phần " + j);
                lesson.setContent("Đây là nội dung bài học số " + j + " của khóa " + topics[i] +
                        ". Nội dung này sẽ được AI sử dụng để tạo câu hỏi trắc nghiệm.");
                lesson.setOrderIndex(j);
                lesson.setCourse(savedCourse);
                lessons.add(lesson);
            }
            lessonRepository.saveAll(lessons);
        }
    }

    private User createUser(String username, String email, String password, String role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        return user;
    }
}
