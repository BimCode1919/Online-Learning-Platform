package org.oln.onlinelearningplatform.seeder;

import lombok.RequiredArgsConstructor;
import org.oln.onlinelearningplatform.entity.Course;
import org.oln.onlinelearningplatform.entity.User;
import org.oln.onlinelearningplatform.repository.CourseRepository;
import org.oln.onlinelearningplatform.repository.UserRepository;
import org.oln.onlinelearningplatform.service.course.CourseService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CourseSeeder {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public void seed() {
        if (courseRepository.count() > 0) return;

        // Đảm bảo email này trùng với email Instructor bạn đã seed trong UserSeeder
        String instructorEmail = "teacher@gmail.com";
        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new RuntimeException("Seeder Error: Không tìm thấy giảng viên " + instructorEmail));

        List<Course> courses = new ArrayList<>();

        // Danh sách 10 khóa học mẫu
        courses.add(createCourse("Java Spring Boot Masterclass", "Lộ trình từ cơ bản đến nâng cao, xây dựng RESTful API chuyên nghiệp.", 499000.0, instructor));
        courses.add(createCourse("React JS for Professionals", "Học cách xây dựng giao diện người dùng hiện đại với Hooks và Redux.", 250000.0, instructor));
        courses.add(createCourse("Next.js Fullstack Web Development", "Xây dựng ứng dụng web tối ưu SEO với SSR và App Router mới nhất.", 350000.0, instructor));
        courses.add(createCourse("Python for Data Science", "Phân tích dữ liệu, vẽ biểu đồ và nhập môn trí tuệ nhân tạo AI.", 300000.0, instructor)); // Khóa miễn phí
        courses.add(createCourse("Docker & Kubernetes Simplified", "Triển khai ứng dụng lên Container và quản lý hệ thống quy mô lớn.", 550000.0, instructor));
        courses.add(createCourse("Microservices Architecture with Spring Cloud", "Thiết kế hệ thống phân tán và giao tiếp giữa các dịch vụ.", 750000.0, instructor));
        courses.add(createCourse("Modern JavaScript ES6+", "Nắm vững các tính năng mới nhất của JS để học các Framework dễ dàng hơn.", 100000.0, instructor)); // Khóa miễn phí
        courses.add(createCourse("C++ Game Development with Unreal Engine", "Phát triển game 3D chất lượng cao với ngôn ngữ C++ mạnh mẽ.", 890000.0, instructor));
        courses.add(createCourse("Database Design & SQL Optimization", "Thiết kế cơ sở dữ liệu chuẩn hóa và tối ưu truy vấn hiệu năng cao.", 120000.0, instructor));
        courses.add(createCourse("AWS Cloud Computing Foundation", "Nhập môn điện toán đám mây và các dịch vụ cơ bản của Amazon Web Services.", 300000.0, instructor));

        courseRepository.saveAll(courses);
        System.out.println(">>> [CourseSeeder]: Đã seed thành công 10 khóa học.");
    }

    private Course createCourse(String title, String desc, Double price, User instructor) {
        Course course = new Course();
        course.setTitle(title);
        course.setDescription(desc);
        course.setPrice(price);
        course.setInstructor(instructor);
        course.setStatus("APPROVE"); // Cho hiện lên trang chủ luôn
        course.setLessons(new ArrayList<>()); // Khởi tạo list rỗng theo yêu cầu
        course.setEnrollments(new ArrayList<>());
        return course;
    }
}