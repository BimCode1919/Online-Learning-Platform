package org.oln.onlinelearningplatform.service.course.impl;

import org.oln.onlinelearningplatform.dto.CourseProgressDTO;
import org.oln.onlinelearningplatform.dto.DashboardStatsDTO;
import org.oln.onlinelearningplatform.entity.*;
import org.oln.onlinelearningplatform.repository.*;
import org.oln.onlinelearningplatform.service.course.CourseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final UserProgressRepository userProgressRepository;
    private final UserRepository userRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    public CourseServiceImpl(CourseRepository courseRepository,
                             LessonRepository lessonRepository,
                             UserProgressRepository userProgressRepository,
                             UserRepository userRepository,
                             QuizAttemptRepository quizAttemptRepository) {
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.userProgressRepository = userProgressRepository;
        this.userRepository = userRepository;
        this.quizAttemptRepository = quizAttemptRepository;
    }

    @Override
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Override
    public Optional<Course> getCourseById(Long courseId) {
        return courseRepository.findById(courseId);
    }

    @Override
    public List<Lesson> getLessonsByCourseId(Long courseId) {
        Optional<Course> courseOpt = courseRepository.findById(courseId);

        if (courseOpt.isPresent()) {
            Course course = courseOpt.get();
            return course.getLessons();
        }

        return List.of();
    }

    @Override
    public Optional<Lesson> getLessonById(Long lessonId) {
        return lessonRepository.findById(lessonId);
    }

    @Override
    public boolean isLessonCompletedByUser(Long userId, Long lessonId) {
        Optional<UserProgress> progressOpt = userProgressRepository.findByUserIdAndLessonId(userId, lessonId);

        return progressOpt.isPresent() && Boolean.TRUE.equals(progressOpt.get().getIsCompleted());
    }

    @Override
    @Transactional
    public void markLessonAsCompleted(Long userId, Long lessonId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson không tồn tại"));

        Optional<UserProgress> progressOpt = userProgressRepository.findByUserIdAndLessonId(userId, lessonId);

        if (progressOpt.isPresent()) {
            UserProgress progress = progressOpt.get();
            progress.setIsCompleted(true);
            progress.setUpdatedAt(LocalDateTime.now());
            userProgressRepository.save(progress);
        } else {
            UserProgress newProgress = new UserProgress();
            newProgress.setUser(user);
            newProgress.setLesson(lesson);
            newProgress.setIsCompleted(true);
            newProgress.setUpdatedAt(LocalDateTime.now());
            userProgressRepository.save(newProgress);
        }
    }

    @Override
    public CourseProgressDTO getCourseProgress(Long userId, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course không tồn tại"));

        List<Lesson> lessons = course.getLessons();
        int totalLessons = lessons.size();

        int completedLessons = 0;
        for (Lesson lesson : lessons) {
            if (isLessonCompletedByUser(userId, lesson.getId())) {
                completedLessons++;
            }
        }

        return new CourseProgressDTO(course, totalLessons, completedLessons);
    }

    @Override
    public DashboardStatsDTO getDashboardStats(Long userId) {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        // 1. Lấy tất cả courses
        List<Course> allCourses = courseRepository.findAll();

        // 2. Tính progress cho từng course
        List<CourseProgressDTO> coursesProgress = new ArrayList<>();
        int totalLessonsCompleted = 0;

        for (Course course : allCourses) {
            CourseProgressDTO progress = getCourseProgress(userId, course.getId());

            // Chỉ add courses mà student đã bắt đầu học (có ít nhất 1 lesson completed)
            if (progress.getCompletedLessons() > 0) {
                coursesProgress.add(progress);
                totalLessonsCompleted += progress.getCompletedLessons();
            }
        }

        stats.setTotalCoursesEnrolled(coursesProgress.size());
        stats.setTotalLessonsCompleted(totalLessonsCompleted);
        stats.setCoursesProgress(coursesProgress);

        // 3. Tính quiz stats (nếu có)
        List<QuizAttempt> quizAttempts = quizAttemptRepository.findAll().stream()
                .filter(attempt -> attempt.getUser().getId().equals(userId))
                .toList();

        stats.setTotalQuizzesTaken(quizAttempts.size());

        if (!quizAttempts.isEmpty()) {
            double avgScore = quizAttempts.stream()
                    .mapToDouble(QuizAttempt::getScore)
                    .average()
                    .orElse(0.0);
            stats.setAverageQuizScore(avgScore);
        }

        return stats;
    }
}