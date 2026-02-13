package org.oln.onlinelearningplatform.service.course.impl;

import org.oln.onlinelearningplatform.entity.*;
import org.oln.onlinelearningplatform.repository.*;
import org.oln.onlinelearningplatform.service.course.CourseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    // --- PHẦN 1: LOGIC CŨ CHO STUDENT (Giữ nguyên) ---

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
        List<Course> allCourses = courseRepository.findAll();
        List<CourseProgressDTO> coursesProgress = new ArrayList<>();
        int totalLessonsCompleted = 0;

        for (Course course : allCourses) {
            CourseProgressDTO progress = getCourseProgress(userId, course.getId());
            if (progress.getCompletedLessons() > 0) {
                coursesProgress.add(progress);
                totalLessonsCompleted += progress.getCompletedLessons();
            }
        }

        stats.setTotalCoursesEnrolled(coursesProgress.size());
        stats.setTotalLessonsCompleted(totalLessonsCompleted);
        stats.setCoursesProgress(coursesProgress);

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

    // --- PHẦN 2: LOGIC MỚI CHO INSTRUCTOR (LUỒNG 1 - ĐÃ SỬA LỖI) ---

    @Override
    @Transactional
    public Course createCourse(String title, String description, String instructorEmail) {
        // 1. Tìm giảng viên
        User instructor = userRepository.findByUsername(instructorEmail)
                .or(() -> userRepository.findByEmail(instructorEmail))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giảng viên: " + instructorEmail));

        // 2. Thiết lập đối tượng
        Course course = new Course();
        course.setTitle(title);
        course.setDescription(description);
        course.setInstructor(instructor);

        // 3. Lưu và trả về đối tượng đã có ID từ Database
        return courseRepository.save(course);
    }

    @Override
    @Transactional
    public Lesson addLesson(Long courseId, String title, String content, Integer orderIndex, String instructorEmail) {
        // 1. Tìm khóa học
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Khóa học không tồn tại ID: " + courseId));

        // 2. Bảo mật: Check quyền
        String ownerEmail = course.getInstructor().getEmail();
        if (ownerEmail == null) ownerEmail = course.getInstructor().getUsername();

        if (!ownerEmail.equals(instructorEmail)) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa khóa học của người khác!");
        }

        // 3. Tạo bài học
        Lesson lesson = new Lesson();
        lesson.setTitle(title);
        lesson.setContent(content); // Input cho AI

        int nextOrder = (orderIndex != null) ? orderIndex : (course.getLessons().size() + 1);
        lesson.setOrderIndex(nextOrder);
        lesson.setCourse(course);

        return lessonRepository.save(lesson);
    }

    @Override
    public List<Course> getCoursesByInstructorEmail(String email) {
        User instructor = userRepository.findByUsername(email)
                .or(() -> userRepository.findByEmail(email))
                .orElseThrow(() -> new RuntimeException("User not found"));

        //Gọi đúng hàm đã khai báo trong Repository
        return courseRepository.findByInstructorIdOrderByIdDesc(instructor.getId());
    }

    @Override
    @Transactional
    public void addOrUpdateLesson(Long courseId, Long lessonId, String title, String content, String videoUrl) {
        // 1. Luôn cần tìm Course cha để đảm bảo tính hợp lệ
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học ID: " + courseId));

        String cleanId = extractYoutubeId(videoUrl);
        Lesson lesson;

        if (lessonId != null) {
            // --- LOGIC CHỈNH SỬA (UPDATE) ---
            lesson = lessonRepository.findById(lessonId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bài học ID: " + lessonId));

            lesson.setTitle(title);
            lesson.setContent(content);
            lesson.setVideoUrl(cleanId);
            // Không cần set lại Course vì nó đã thuộc về Course này rồi
        } else {
            // --- LOGIC TẠO MỚI (INSERT) ---
            lesson = new Lesson();
            lesson.setTitle(title);
            lesson.setContent(content);
            lesson.setVideoUrl(cleanId);
            lesson.setCourse(course); // Gắn bài học vào khóa học
        }

        // 2. Lưu (JPA sẽ tự động Update nếu object có ID và Insert nếu chưa có ID)
        lessonRepository.save(lesson);
    }

    private String extractYoutubeId(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        // Regex mạnh mẽ để tách ID từ nhiều định dạng link YouTube (shorter, watch, embed, v.v.)
        String pattern = "(?<=watch\\?v=|/videos/|/embed/|youtu.be/|/v/|/e/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%2F|youtu.be%2F|%2Fv%2F)[^#&?\\n]*";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url);

        if (matcher.find()) {
            return matcher.group();
        }

        // Nếu link đã là ID rồi (ví dụ: dQw4w9WgXcQ) thì trả về chính nó
        return url;
    }

    @Override
    @Transactional
    public void deleteLesson(Long lessonId) {
        lessonRepository.deleteById(lessonId);
    }

    @Override
    @Transactional
    public Course saveOrUpdateCourse(Course course, String instructorEmail) {
        User instructor = userRepository.findByUsername(instructorEmail)
                .or(() -> userRepository.findByEmail(instructorEmail))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giảng viên"));

        if (course.getId() != null) {
            // --- LOGIC CẬP NHẬT ---
            Course existingCourse = courseRepository.findById(course.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học để cập nhật"));
            if ("FIX".equals(course.getStatus())) {
                course.setStatus("PENDING"); // Gửi lại cho Admin
            }
            existingCourse.setTitle(course.getTitle());
            existingCourse.setDescription(course.getDescription());

            // Khi cập nhật thông tin, thường ta giữ nguyên trạng thái hiện tại
            existingCourse.setStatus("PENDING");

            return courseRepository.save(existingCourse);
        } else {
            course.setInstructor(instructor);
            course.setStatus("PENDING");

            return courseRepository.save(course);
        }
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        // 1. Kiểm tra tồn tại
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khóa học không tồn tại"));

        // 2. Thực hiện xóa (Cascade sẽ tự lo phần Lessons)
        courseRepository.delete(course);
    }

    @Override
    public List<Course> getCoursesByStatus(String status) {
        // Gọi Repository để tìm các khóa học theo trạng thái (PENDING, APPROVE,...)
        return courseRepository.findByStatus(status);
    }

    @Override
    @Transactional
    public void updateCourseStatus(Long id, String newStatus) {
        // 1. Tìm khóa học theo ID
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học với ID: " + id));

        // 2. Cập nhật trạng thái mới (Ví dụ: từ "PENDING" sang "APPROVE")
        course.setStatus(newStatus);

        // 3. Lưu lại thay đổi
        courseRepository.save(course);
    }

    @Override
    public void save (Course course) {
        courseRepository.save(course);
    }
}