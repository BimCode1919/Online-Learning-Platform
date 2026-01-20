//package org.oln.onlinelearningplatform.seeder;
//
//import org.oln.onlinelearningplatform.entity.*;
//import org.oln.onlinelearningplatform.repository.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.Arrays;
//
//@Component
//public class DataSeeder implements CommandLineRunner {
//
//    @Autowired private UserRepository userRepository;
//    @Autowired private CourseRepository courseRepository;
//    @Autowired private LessonRepository lessonRepository;
//    @Autowired private AIQuizRepository aiQuizRepository;
//    @Autowired private QuestionRepository questionRepository;
//    @Autowired private OptionRepository optionRepository;
//    @Autowired private QuizAttemptRepository quizAttemptRepository;
//    @Autowired private UserProgressRepository userProgressRepository;
//
//    @Override
//    @Transactional
//    public void run(String... args) throws Exception {
//        if (userRepository.count() > 0) return;
//
//        // --- LUỒNG 6: SEED USER (Phân quyền) ---
//        User admin = new User();
//        admin.setUsername("admin");
//        admin.setPassword("123"); // Lưu ý: Nếu dùng Spring Security thật thì cần encode password
//        admin.setRole("ADMIN");
//        userRepository.save(admin);
//
//        User instructor = new User();
//        instructor.setUsername("giangvien");
//        instructor.setPassword("123");
//        instructor.setRole("INSTRUCTOR");
//        userRepository.save(instructor);
//
//        User student = new User();
//        student.setUsername("sinhvien");
//        student.setPassword("123");
//        student.setRole("STUDENT");
//        userRepository.save(student);
//
//        // --- LUỒNG 1 & 2: SEED COURSE & LESSON (Nội dung cho AI đọc) ---
//        Course course = new Course();
//        course.setTitle("Lập trình Java Spring Boot cho người mới");
//        course.setDescription("Khóa học hướng dẫn từ Zero đến Hero về Spring Framework.");
//        course.setInstructor(instructor);
//        courseRepository.save(course);
//
//        Lesson lesson1 = new Lesson();
//        lesson1.setTitle("Giới thiệu về Dependency Injection");
//        lesson1.setContent("Dependency Injection (DI) là một design pattern quan trọng trong Spring. " +
//                "Nó giúp giảm sự phụ thuộc giữa các class (Loose Coupling). " +
//                "Thay vì tạo đối tượng bằng từ khóa 'new' bên trong class, Spring Container sẽ 'tiêm' (inject) các phụ thuộc vào.");
//        lesson1.setOrderIndex(1);
//        lesson1.setCourse(course);
//        lessonRepository.save(lesson1);
//
//        Lesson lesson2 = new Lesson();
//        lesson2.setTitle("Tìm hiểu về Spring Bean và ApplicationContext");
//        lesson2.setContent("Trong Spring, các đối tượng được quản lý bởi Spring IoC Container được gọi là Beans. " +
//                "ApplicationContext là interface đại diện cho IoC Container, có nhiệm vụ khởi tạo, cấu hình và quản lý vòng đời của Bean.");
//        lesson2.setOrderIndex(2);
//        lesson2.setCourse(course);
//        lessonRepository.save(lesson2);
//
//        // --- LUỒNG 3: SEED AI QUIZ (Dữ liệu mẫu để test giao diện) ---
//        AIQuiz quiz1 = new AIQuiz();
//        quiz1.setLesson(lesson1);
//        quiz1.setStatus("READY");
//        quiz1.setDifficulty("MEDIUM");
//        aiQuizRepository.save(quiz1);
//
//        // Seed câu hỏi mẫu (Để Luồng 4 - Chấm điểm chạy được ngay)
//        Question q1 = new Question();
//        q1.setQuiz(quiz1);
//        q1.setQuestionText("Mục tiêu chính của Dependency Injection là gì?");
//        q1.setExplanation("DI giúp tách rời việc tạo đối tượng khỏi logic nghiệp vụ, làm code dễ test hơn.");
//        questionRepository.save(q1);
//
//        Option op1 = new Option();
//        op1.setQuestion(q1);
//        op1.setOptionText("Giúp tăng sự phụ thuộc");
//        op1.setIsCorrect(false);
//
//        Option op2 = new Option();
//        op2.setQuestion(q1);
//        op2.setOptionText("Giảm sự phụ thuộc (Loose Coupling)");
//        op2.setIsCorrect(true);
//
//        optionRepository.saveAll(Arrays.asList(op1, op2));
//
//        // --- LUỒNG 4 & 5: SEED KẾT QUẢ (Để test AI Feedback) ---
//        QuizAttempt attempt = new QuizAttempt();
//        attempt.setUser(student);
//        attempt.setQuiz(quiz1);
//        attempt.setScore(5.0f);
//        attempt.setAiFeedback("Bạn đã hiểu khái niệm DI nhưng cần chú ý hơn về cách áp dụng @Autowired.");
//        attempt.setCreatedAt(LocalDateTime.now().minusDays(1));
//        quizAttemptRepository.save(attempt);
//
//        // --- LUỒNG 7: SEED TIẾN ĐỘ (Để AI gợi ý lộ trình) ---
//        UserProgress progress = new UserProgress();
//        progress.setUser(student);
//        progress.setLesson(lesson1);
//        progress.setIsCompleted(true);
//        progress.setUpdatedAt(LocalDateTime.now());
//        userProgressRepository.save(progress);
//
//        System.out.println(">>>>> DỮ LIỆU MẪU ĐÃ ĐƯỢC SEED THÀNH CÔNG! <<<<<");
//    }
//}
