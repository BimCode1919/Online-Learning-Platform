package org.oln.onlinelearningplatform.repository;

import org.oln.onlinelearningplatform.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
}
