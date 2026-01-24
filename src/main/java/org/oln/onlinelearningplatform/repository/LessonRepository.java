package org.oln.onlinelearningplatform.repository;

import org.oln.onlinelearningplatform.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    // Tìm tất cả bài học của 1 khóa học và sắp xếp theo thứ tự (Bài 1, Bài 2...)
    List<Lesson> findByCourseIdOrderByOrderIndexAsc(Long courseId);
}