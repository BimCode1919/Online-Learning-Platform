package org.oln.onlinelearningplatform.repository;

import org.oln.onlinelearningplatform.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    // Tìm khóa học theo ID giảng viên và SẮP XẾP giảm dần theo ID (Mới nhất lên đầu)
    List<Course> findByInstructorIdOrderByIdDesc(Long instructorId);

    List<Course> findByStatus(String status);
}