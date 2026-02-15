package org.oln.onlinelearningplatform.repository;

import org.oln.onlinelearningplatform.entity.Course;
import org.oln.onlinelearningplatform.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    Optional<Lesson> findByCourseIdAndTitle(Long courseId, String title);
}