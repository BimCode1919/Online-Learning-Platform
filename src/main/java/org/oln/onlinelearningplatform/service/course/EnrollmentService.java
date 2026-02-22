package org.oln.onlinelearningplatform.service.course;

import org.oln.onlinelearningplatform.entity.Enrollment;

import java.util.List;

public interface EnrollmentService {
    Enrollment findByUserAndCourse(Long userId, Long courseId);
    void save(Enrollment enrollment);
    List<Enrollment> findByUserIdAndPaymentStatus(Long id, String status);
    List<Enrollment> getEnrollmentsByStatus(String status);
    void updateProgress(Enrollment enrollment, Long lessonId);
}
