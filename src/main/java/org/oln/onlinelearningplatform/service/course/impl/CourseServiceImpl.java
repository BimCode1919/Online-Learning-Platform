package org.oln.onlinelearningplatform.service.course.impl;

import org.oln.onlinelearningplatform.entity.Course;
import org.oln.onlinelearningplatform.entity.Lesson;
import org.oln.onlinelearningplatform.entity.User;
import org.oln.onlinelearningplatform.entity.UserProgress;
import org.oln.onlinelearningplatform.repository.CourseRepository;
import org.oln.onlinelearningplatform.repository.LessonRepository;
import org.oln.onlinelearningplatform.repository.UserProgressRepository;
import org.oln.onlinelearningplatform.repository.UserRepository;
import org.oln.onlinelearningplatform.service.course.CourseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final UserProgressRepository userProgressRepository;
    private final UserRepository userRepository;

    public CourseServiceImpl(CourseRepository courseRepository,
                             LessonRepository lessonRepository,
                             UserProgressRepository userProgressRepository,
                             UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.userProgressRepository = userProgressRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<Course> getAllCourses() {
        // Lấy tất cả courses, Spring Data JPA sẽ tự động fetch lessons theo @OneToMany
        return courseRepository.findAll();
    }

    @Override
    public Optional<Course> getCourseById(Long courseId) {
        return courseRepository.findById(courseId);
    }

    @Override
    public List<Lesson> getLessonsByCourseId(Long courseId) {
        // Tìm course trước
        Optional<Course> courseOpt = courseRepository.findById(courseId);

        if (courseOpt.isPresent()) {
            Course course = courseOpt.get();
            // Lessons đã được @OrderBy trong entity Course, nên tự động sắp xếp
            return course.getLessons();
        }

        return List.of(); // Trả về empty list nếu không tìm thấy course
    }

    @Override
    public Optional<Lesson> getLessonById(Long lessonId) {
        return lessonRepository.findById(lessonId);
    }

    @Override
    public boolean isLessonCompletedByUser(Long userId, Long lessonId) {
        // Sử dụng custom query thay vì findAll() - hiệu quả hơn nhiều
        Optional<UserProgress> progressOpt = userProgressRepository.findByUserIdAndLessonId(userId, lessonId);

        return progressOpt.isPresent() && Boolean.TRUE.equals(progressOpt.get().getIsCompleted());
    }

    @Override
    @Transactional
    public void markLessonAsCompleted(Long userId, Long lessonId) {
        // Tìm user và lesson
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson không tồn tại"));

        // Sử dụng custom query để tìm progress
        Optional<UserProgress> progressOpt = userProgressRepository.findByUserIdAndLessonId(userId, lessonId);

        if (progressOpt.isPresent()) {
            // Đã có record → cập nhật
            UserProgress progress = progressOpt.get();
            progress.setIsCompleted(true);
            progress.setUpdatedAt(LocalDateTime.now());
            userProgressRepository.save(progress);
        } else {
            // Chưa có record → tạo mới
            UserProgress newProgress = new UserProgress();
            newProgress.setUser(user);
            newProgress.setLesson(lesson);
            newProgress.setIsCompleted(true);
            newProgress.setUpdatedAt(LocalDateTime.now());
            userProgressRepository.save(newProgress);
        }
    }
}