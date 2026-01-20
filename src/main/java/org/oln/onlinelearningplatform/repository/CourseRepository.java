package org.oln.onlinelearningplatform.repository;

import org.oln.onlinelearningplatform.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
