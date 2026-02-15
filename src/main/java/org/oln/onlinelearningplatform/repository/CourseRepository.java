package org.oln.onlinelearningplatform.repository;

import org.oln.onlinelearningplatform.entity.Course;
import org.oln.onlinelearningplatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    // Tìm khóa học theo ID giảng viên và SẮP XẾP giảm dần theo ID (Mới nhất lên đầu)
    List<Course> findByInstructorIdOrderByIdDesc(Long instructorId);

    List<Course> findByStatus(String status);

    List<Course> getCoursesByInstructor(User instructor);

    @Query("SELECT DISTINCT c FROM Course c LEFT JOIN FETCH c.lessons")
    List<Course> findAllWithLessons();
}