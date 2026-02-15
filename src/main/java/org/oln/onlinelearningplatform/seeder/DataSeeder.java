package org.oln.onlinelearningplatform.seeder;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1) // Chạy đầu tiên khi ứng dụng khởi động
public class DataSeeder implements CommandLineRunner {

    private final UserSeeder userSeeder;
    private final CourseSeeder courseSeeder;
    private final LessonSeeder lessonSeeder;

    // Sử dụng Constructor Injection (DI)
    public DataSeeder(UserSeeder userSeeder, CourseSeeder courseSeeder, LessonSeeder lessonSeeder) {
        this.userSeeder = userSeeder;
        this.courseSeeder = courseSeeder;
        this.lessonSeeder = lessonSeeder;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> Đang khởi tạo dữ liệu mẫu (Seeding)...");

        // Chạy theo thứ tự: User trước, Course sau (vì Course cần Instructor)
        userSeeder.seed();
        courseSeeder.seed();
        lessonSeeder.seed();

        System.out.println(">>> Hoàn tất khởi tạo dữ liệu!");
    }
}