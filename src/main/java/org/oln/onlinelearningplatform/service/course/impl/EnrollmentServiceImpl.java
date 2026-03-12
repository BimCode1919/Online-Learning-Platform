package org.oln.onlinelearningplatform.service.course.impl;

import org.oln.onlinelearningplatform.entity.Course;
import org.oln.onlinelearningplatform.entity.Enrollment;
import org.oln.onlinelearningplatform.entity.User;
import org.oln.onlinelearningplatform.repository.EnrollmentRepository;
import org.oln.onlinelearningplatform.service.course.EnrollmentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final org.oln.onlinelearningplatform.repository.UserProgressRepository userProgressRepository;
    private final org.oln.onlinelearningplatform.repository.LessonRepository lessonRepository;

    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository,
                                 org.oln.onlinelearningplatform.repository.UserProgressRepository userProgressRepository,
                                 org.oln.onlinelearningplatform.repository.LessonRepository lessonRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.userProgressRepository = userProgressRepository;
        this.lessonRepository = lessonRepository;
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
        if (enrollment == null || lessonId == null) {
            return;
        }

        User user = enrollment.getUser();
        Course course = enrollment.getCourse();

        // 1. Save UserProgress
        java.util.Optional<org.oln.onlinelearningplatform.entity.UserProgress> progressOpt = 
                userProgressRepository.findByUserIdAndLessonId(user.getId(), lessonId);
        
        org.oln.onlinelearningplatform.entity.UserProgress progress;
        if (progressOpt.isPresent()) {
            progress = progressOpt.get();
        } else {
            progress = new org.oln.onlinelearningplatform.entity.UserProgress();
            progress.setUser(user);
            
            // We need Lesson, so let's fetch it via lessonRepository or just set reference if possible
            // To be safe, let's fetch it. But we don't have LessonRepository injected yet.
            // Let's inject LessonRepository.
            org.oln.onlinelearningplatform.entity.Lesson lesson = lessonRepository.findById(lessonId).orElse(null);
            if (lesson == null) return;
            progress.setLesson(lesson);
        }

        progress.setIsCompleted(true);
        progress.setUpdatedAt(java.time.LocalDateTime.now());
        userProgressRepository.save(progress);

        // 2. Recalculate Course Progress
        long completedLessons = userProgressRepository.countCompletedLessonsByUserAndCourse(user.getId(), course.getId());
        int totalLessons = course.getLessons() != null ? course.getLessons().size() : 0;

        if (totalLessons > 0) {
            double percentage = ((double) completedLessons / totalLessons) * 100.0;
            // Cap at 100% just in case
            percentage = Math.min(percentage, 100.0);
            enrollment.setProgressPercentage(percentage);
            
            if (percentage >= 100.0) {
                enrollment.setIsCompleted(true);
            }
            
            enrollmentRepository.save(enrollment);
        }
    }
}
