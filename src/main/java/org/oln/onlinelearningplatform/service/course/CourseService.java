package org.oln.onlinelearningplatform.service.course;

import org.oln.onlinelearningplatform.entity.Course;
import org.oln.onlinelearningplatform.entity.CourseProgressDTO;
import org.oln.onlinelearningplatform.entity.DashboardStatsDTO;
import org.oln.onlinelearningplatform.entity.Lesson;

import java.util.List;
import java.util.Optional;

public interface CourseService {

    // Lấy tất cả courses có sẵn trên hệ thống
    List<Course> getAllCourses();


    // Lấy chi tiết một course theo ID
    Optional<Course> getCourseById(Long courseId);

    // Lấy tất cả lessons của một course (đã sắp xếp theo orderIndex)

    List<Lesson> getLessonsByCourseId(Long courseId);

    // Lấy chi tiết một lesson theo ID

    Optional<Lesson> getLessonById(Long lessonId);

    // Kiểm tra xem student đã hoàn thành lesson chưa
    boolean isLessonCompletedByUser(Long userId, Long lessonId);

    // Đánh dấu lesson là đã hoàn thành

    void markLessonAsCompleted(Long userId, Long lessonId);

    /**
     * Lấy progress của user cho một course cụ thể
     * @param userId ID của student
     * @param courseId ID của course
     * @return CourseProgressDTO
     */
    CourseProgressDTO getCourseProgress(Long userId, Long courseId);

    /**
     * Lấy tất cả thống kê dashboard cho student
     * @param userId ID của student
     * @return DashboardStatsDTO
     */
    DashboardStatsDTO getDashboardStats(Long userId);
}
