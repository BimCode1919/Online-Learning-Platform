package org.oln.onlinelearningplatform.service.course;

import org.oln.onlinelearningplatform.entity.*;

import java.util.List;
import java.util.Optional;

public interface CourseService {

    // --- PHẦN 1: DÀNH CHO STUDENT (Xem & Học) ---

    // Lấy tất cả courses có sẵn trên hệ thống (Trang chủ)
    List<Course> getAllCourses();

    // Lấy chi tiết một course theo ID
    Optional<Course> getCourseById(Long courseId);

    // Kiểm tra xem student đã hoàn thành lesson chưa
    boolean isLessonCompletedByUser(Long userId, Long lessonId);

    CourseProgressDTO getCourseProgress(Long userId, Long courseId);

    DashboardStatsDTO getDashboardStats(Long userId);


    // --- PHẦN 2: DÀNH CHO INSTRUCTOR - LUỒNG 1 (Quản lý nội dung) ---

    List<Course> getCoursesByInstructorEmail(String email);

    void addOrUpdateLesson(Long courseId, Long lessonId, String title, String content, String videoUrl);

    void deleteLesson(Long lessonId);

    Course saveOrUpdateCourse(Course course, String instructorEmail);

    void deleteCourse(Long id);

    List<Course> getCoursesByStatus(String status);

    void updateCourseStatus(Long id, String newStatus);

    void save(Course course);

    List<Course> getCoursesByInstructor(User instructor);
}