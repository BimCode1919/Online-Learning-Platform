package org.oln.onlinelearningplatform.service.course;

import org.oln.onlinelearningplatform.entity.Course;
import org.oln.onlinelearningplatform.entity.CourseProgressDTO;
import org.oln.onlinelearningplatform.entity.DashboardStatsDTO;
import org.oln.onlinelearningplatform.entity.Lesson;

import java.util.List;
import java.util.Optional;

public interface CourseService {

    // --- PHẦN 1: DÀNH CHO STUDENT (Xem & Học) ---

    // Lấy tất cả courses có sẵn trên hệ thống (Trang chủ)
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


    // --- PHẦN 2: DÀNH CHO INSTRUCTOR - LUỒNG 1 (Quản lý nội dung) ---

    /**
     * 1. Tạo khóa học mới
     * @param title Tiêu đề khóa học
     * @param description Mô tả
     * @param instructorEmail Email của giảng viên (để xác định người tạo)
     * @return Course vừa tạo
     */
    Course createCourse(String title, String description, String instructorEmail);

    /**
     * 2. Thêm bài học mới (QUAN TRỌNG: Đây là nguồn dữ liệu cho AI)
     * @param courseId ID khóa học cần thêm bài
     * @param title Tên bài học
     * @param content Nội dung bài học (Văn bản dài - Input cho AI)
     * @param orderIndex Thứ tự bài (1, 2, 3...)
     * @param instructorEmail Email giảng viên (để kiểm tra quyền sở hữu)
     * @return Lesson vừa tạo
     */
    Lesson addLesson(Long courseId, String title, String content, Integer orderIndex, String instructorEmail);

    /**
     * 3. Lấy danh sách khóa học của riêng giảng viên đó (để hiển thị Dashboard giảng viên)
     * @param email Email giảng viên
     * @return List<Course> do giảng viên đó tạo
     */
    List<Course> getCoursesByInstructorEmail(String email);
}