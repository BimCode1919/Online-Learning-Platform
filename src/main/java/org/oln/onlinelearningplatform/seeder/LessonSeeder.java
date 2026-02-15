package org.oln.onlinelearningplatform.seeder;

import lombok.RequiredArgsConstructor;
import org.oln.onlinelearningplatform.entity.Course;
import org.oln.onlinelearningplatform.repository.CourseRepository;
import org.oln.onlinelearningplatform.repository.LessonRepository;
import org.oln.onlinelearningplatform.service.course.CourseService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LessonSeeder {

    private final CourseRepository courseRepository;
    private final CourseService courseService;
    private final LessonRepository lessonRepository; // Cần dùng để cập nhật orderIndex nếu Service chưa có

    public void seed() {
        // Lấy tất cả khóa học
        List<Course> courses = courseRepository.findAllWithLessons();

        if (courses.isEmpty()) {
            System.out.println(">>> [LessonSeeder]: Không tìm thấy khóa học nào.");
            return;
        }

        // Link YouTube mẫu (Cùng 1 clip ZK-rNEhJIDs theo yêu cầu)
        String videoUrl = "https://www.youtube.com/watch?v=ZK-rNEhJIDs";

        for (Course course : courses) {
            // Chỉ seed nếu khóa học chưa có bài học nào
            if (course.getLessons() == null || course.getLessons().isEmpty()) {

                System.out.println(">>> [LessonSeeder]: Đang thêm 3 bài học cho: " + course.getTitle());

                // Sử dụng mảng để lặp cho nhanh và gán orderIndex
                String[] lessonTitles = {
                        "Getting Started with " + course.getTitle(),
                        "Deep Dive into Core Features",
                        "Final Project and Best Practices"
                };

                for (int i = 0; i < lessonTitles.length; i++) {
                    // Gọi logic của bạn (nó sẽ lưu vào DB)
                    courseService.addOrUpdateLesson(
                            course.getId(),
                            null,
                            lessonTitles[i],
                            "Nội dung bài học này sẽ dùng làm input cho AI sau này...",
                            videoUrl
                    );

                    // Vì hàm addOrUpdateLesson của bạn chưa xử lý orderIndex,
                    // chúng ta sẽ lấy bài học vừa lưu để cập nhật orderIndex
                    updateOrderIndex(course.getId(), lessonTitles[i], i + 1);
                }
            }
        }
    }

    /**
     * Helper để gán orderIndex vì trong Service của bạn chưa có
     */
    private void updateOrderIndex(Long courseId, String title, int index) {
        // Tìm lại bài học vừa add dựa trên course và title
        lessonRepository.findByCourseIdAndTitle(courseId, title).ifPresent(l -> {
            l.setOrderIndex(index);
            lessonRepository.save(l);
        });
    }
}
