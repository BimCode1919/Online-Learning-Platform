package org.oln.onlinelearningplatform.service.course.impl;

import org.oln.onlinelearningplatform.entity.Enrollment;
import org.oln.onlinelearningplatform.repository.EnrollmentRepository;
import org.oln.onlinelearningplatform.service.course.EnrollmentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    @Override
    public Enrollment findByUserAndCourse(Long userId, Long courseId) {
        return enrollmentRepository.findByUserIdAndCourseId(userId, courseId).orElse(null);
    }

    @Override
    public void save(Enrollment enrollment) {
        enrollmentRepository.save(enrollment);
    }

    @Override
    public List<Enrollment> findByUserIdAndPaymentStatus(Long id, String status){
        return enrollmentRepository.findByUserIdAndPaymentStatus(id, status);
    }

    @Override
    public List<Enrollment> getEnrollmentsByStatus(String status){
        return enrollmentRepository.findByPaymentStatus(status);
    }

    @Override
    public void updateProgress(Enrollment enrollment, Long lessonId) {

    }
}
