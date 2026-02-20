package org.oln.onlinelearningplatform.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.oln.onlinelearningplatform.entity.*;
import org.oln.onlinelearningplatform.repository.EnrollmentRepository;
import org.oln.onlinelearningplatform.repository.QuizAttemptRepository;
import org.oln.onlinelearningplatform.repository.QuizRepository;
import org.oln.onlinelearningplatform.service.course.CourseService;
import org.oln.onlinelearningplatform.service.course.EnrollmentService;
import org.oln.onlinelearningplatform.service.payment.VNPayService;
import org.oln.onlinelearningplatform.service.quiz.QuizAttemptService;
import org.oln.onlinelearningplatform.service.user.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/student")
public class StudentController {

    private final CourseService courseService;
    private final UserService userService;
    private final EnrollmentService enrollmentService;
    private final VNPayService vnPayService;
    private final EnrollmentRepository enrollmentRepository;
    private final QuizRepository quizRepository;
    private final QuizAttemptService quizAttemptService;
    private final QuizAttemptRepository quizAttemptRepository;

    public StudentController(CourseService courseService,
                             UserService userService,
                             EnrollmentService enrollmentService,
                             VNPayService vnPayService,
                             EnrollmentRepository enrollmentRepository,
                             QuizRepository quizRepository,
                             QuizAttemptService quizAttemptService,
                             QuizAttemptRepository quizAttemptRepository) {
        this.courseService = courseService;
        this.userService = userService;
        this.enrollmentService = enrollmentService;
        this.vnPayService = vnPayService;
        this.enrollmentRepository = enrollmentRepository;
        this.quizRepository = quizRepository;
        this.quizAttemptService = quizAttemptService;
        this.quizAttemptRepository = quizAttemptRepository;
    }

    // ============= DASHBOARD =============
    @GetMapping("/dashboard")
    public String viewDashboard(Model model,
                                @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        User user = userService.findByEmail(email);
        model.addAttribute("currentUser", user);

        DashboardStatsDTO stats = courseService.getDashboardStats(user.getId());
        model.addAttribute("stats", stats);

        return "views/student/dashboard";
    }

    // ============= DANH SÁCH KHÓA HỌC =============
    @GetMapping("/courses")
    public String listAvailableCourses(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        User user = userService.findByEmail(email);
        model.addAttribute("currentUser", user);

        List<Course> availableCourses = courseService.getCoursesByStatus("APPROVE");
        model.addAttribute("availableCourses", availableCourses);

        return "views/student/courses";
    }

    // ============= CHI TIẾT KHÓA HỌC =============
    @GetMapping("/course/detail/{id}")
    public String viewCourseDetail(@PathVariable Long id,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   Model model) {
        String email = userDetails.getUsername();
        User user = userService.findByEmail(email);

        Course course = courseService.getCourseById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học với ID: " + id));

        Enrollment enrollment = enrollmentService.findByUserAndCourse(user.getId(), id);

        model.addAttribute("course", course);
        model.addAttribute("enrollment", enrollment);
        model.addAttribute("currentUser", user);

        return "views/student/course-detail";
    }

    // ============= ĐĂNG KÝ KHÓA HỌC =============
    @PostMapping("/course/enroll/{id}")
    public String handleEnrollment(@PathVariable Long id,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   HttpServletRequest request) {
        User user = userService.findByEmail(userDetails.getUsername());
        Course course = courseService.getCourseById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học với ID: " + id));

        Enrollment existing = enrollmentService.findByUserAndCourse(user.getId(), course.getId());
        if (existing != null && "COMPLETED".equals(existing.getPaymentStatus())) {
            return "redirect:/student/learning/" + id;
        }

        Enrollment enrollment = (existing != null) ? existing : new Enrollment();
        enrollment.setUser(user);
        enrollment.setCourse(course);

        double price = course.getPrice();
        enrollment.setTotalAmount(price);
        enrollment.setAdminCommission(price * 0.05);
        enrollment.setInstructorShare(price * 0.95);
        enrollment.setPaymentStatus("PENDING");
        enrollment.setVnpTxnRef("DEV" + System.currentTimeMillis());

        enrollmentService.save(enrollment);

        try {
            String paymentUrl = vnPayService.createPaymentUrl(enrollment, request);
            return "redirect:" + paymentUrl;
        } catch (UnsupportedEncodingException e) {
            return "redirect:/student/course/detail/" + id + "?error=payment_failed";
        }
    }

    // ============= CALLBACK THANH TOÁN =============
    @GetMapping("/payment-callback")
    public String paymentCallback(HttpServletRequest request) {
        String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
        String vnp_TxnRef = request.getParameter("vnp_TxnRef");

        Enrollment enrollment = enrollmentRepository.findByVnpTxnRef(vnp_TxnRef).orElse(null);

        if (enrollment != null) {
            if ("00".equals(vnp_ResponseCode)) {
                enrollment.setPaymentStatus("COMPLETED");
                enrollmentRepository.save(enrollment);
                return "redirect:/student/payment-success";
            } else {
                enrollmentRepository.delete(enrollment);
                return "redirect:/student/course/detail/" + enrollment.getCourse().getId() + "?error=canceled";
            }
        }
        return "redirect:/student/courses";
    }

    @GetMapping("/payment-success")
    public String paymentSuccess() {
        return "views/student/payment-success";
    }

    // ============= KHÓA HỌC CỦA TÔI =============
    @GetMapping("/my-courses")
    public String showMyCourses(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername());

        List<Enrollment> myEnrollments = enrollmentService.findByUserIdAndPaymentStatus(user.getId(), "COMPLETED");
        model.addAttribute("enrollments", myEnrollments);
        return "views/student/my-courses-list";
    }

    // ============= TRANG HỌC TẬP =============
    @GetMapping("/learning/{courseId}")
    public String startLearning(@PathVariable Long courseId,
                                @AuthenticationPrincipal UserDetails userDetails,
                                Model model) {
        User user = userService.findByEmail(userDetails.getUsername());
        Enrollment enrollment = enrollmentService.findByUserAndCourse(user.getId(), courseId);

        if (enrollment == null || !"COMPLETED".equals(enrollment.getPaymentStatus())) {
            return "redirect:/student/course/detail/" + courseId + "?error=not_purchased";
        }

        Course course = courseService.getCourseById(courseId)
                .orElseThrow(() -> new RuntimeException("Khóa học không tồn tại"));

        // Lấy danh sách quiz đã hoàn thành
        Set<Long> completedQuizzes = quizAttemptService.findCompletedQuizIdsByUserAndCourse(user.getId(), courseId);

        // Lấy tất cả kết quả quiz của user trong khóa học này
        List<QuizAttempt> quizAttempts = quizAttemptService.findByUserAndCourse(user.getId(), courseId);

        model.addAttribute("course", course);
        model.addAttribute("enrollment", enrollment);
        model.addAttribute("completedQuizzes", completedQuizzes);
        model.addAttribute("quizAttempts", quizAttempts);

        return "views/student/learning-dashboard";
    }

    // ============= PHẦN QUIZ CHO STUDENT =============

    /**
     * Trang làm quiz
     */
    @GetMapping("/quiz/{quizId}/take")
    public String takeQuiz(@PathVariable Long quizId,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model,
                           RedirectAttributes redirectAttributes) {

        User user = userService.findByEmail(userDetails.getUsername());

        // Debug: Kiểm tra quizId
        System.out.println("=== TAKE QUIZ ===");
        System.out.println("Quiz ID nhận được: " + quizId);

        // Lấy quiz với tất cả dữ liệu
        Quiz quiz = quizRepository.findByIdWithQuestionsAndOptions(quizId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quiz với ID: " + quizId));

        // Debug: Kiểm tra dữ liệu quiz
        System.out.println("Quiz tìm thấy: " + (quiz != null ? quiz.getId() : "null"));
        System.out.println("Quiz lesson: " + (quiz.getLesson() != null ? quiz.getLesson().getTitle() : "null"));
        System.out.println("Quiz course: " + (quiz.getLesson() != null && quiz.getLesson().getCourse() != null ?
                quiz.getLesson().getCourse().getTitle() : "null"));
        System.out.println("Questions size: " + (quiz.getQuestions() != null ? quiz.getQuestions().size() : 0));

        // Kiểm tra null trước khi truy cập
        if (quiz.getLesson() == null) {
            redirectAttributes.addFlashAttribute("error", "Quiz không có bài học liên kết!");
            return "redirect:/student/courses";
        }

        if (quiz.getLesson().getCourse() == null) {
            redirectAttributes.addFlashAttribute("error", "Bài học không có khóa học liên kết!");
            return "redirect:/student/courses";
        }

        // Kiểm tra học viên đã đăng ký khóa học chưa
        Enrollment enrollment = enrollmentService.findByUserAndCourse(user.getId(),
                quiz.getLesson().getCourse().getId());

        if (enrollment == null || !"COMPLETED".equals(enrollment.getPaymentStatus())) {
            redirectAttributes.addFlashAttribute("error", "Bạn chưa đăng ký khóa học này!");
            return "redirect:/student/courses";
        }

        // Kiểm tra đã làm quiz này chưa
        Optional<QuizAttempt> existingAttempt = quizAttemptRepository.findByUserAndQuiz(user, quiz);
        if (existingAttempt.isPresent()) {
            return "redirect:/student/quiz/result/" + existingAttempt.get().getId();
        }

        model.addAttribute("quiz", quiz);
        return "views/student/take-quiz";
    }

    /**
     * Xử lý nộp bài quiz
     */
    @PostMapping("/quiz/{quizId}/submit")
    public String submitQuiz(@PathVariable Long quizId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             @RequestParam(value = "selectedOptions", required = false) List<Long> selectedOptions,
                             RedirectAttributes redirectAttributes) {

        // Log để debug
        System.out.println("=== SUBMIT QUIZ ===");
        System.out.println("Quiz ID: " + quizId);
        System.out.println("Selected options: " + (selectedOptions != null ? selectedOptions : "null"));

        if (selectedOptions == null || selectedOptions.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Bạn chưa chọn đáp án nào!");
            return "redirect:/student/quiz/" + quizId + "/take";
        }

        User user = userService.findByEmail(userDetails.getUsername());
        Quiz quiz = quizRepository.findByIdWithQuestionsAndOptions(quizId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quiz"));

        // Tính điểm
        int totalQuestions = quiz.getQuestions().size();
        int correctCount = 0;

        for (Question question : quiz.getQuestions()) {
            Option correctOption = question.getOptions().stream()
                    .filter(Option::isCorrect)
                    .findFirst()
                    .orElse(null);

            if (correctOption != null && selectedOptions.contains(correctOption.getId())) {
                correctCount++;
            }
        }

        float score = (float) correctCount / totalQuestions * 100;

        // Tạo feedback
        String feedback = generateFeedback(score);

        // Lưu kết quả
        QuizAttempt attempt = new QuizAttempt();
        attempt.setUser(user);
        attempt.setQuiz(quiz);
        attempt.setScore(score);
        attempt.setTotalQuestions(totalQuestions);
        attempt.setCorrectAnswers(correctCount);
        attempt.setAiFeedback(feedback);
        attempt.setCreatedAt(LocalDateTime.now());

        quizAttemptRepository.save(attempt);

        redirectAttributes.addFlashAttribute("success", "Hoàn thành quiz! Điểm: " + Math.round(score) + "%");
        return "redirect:/student/quiz/result/" + attempt.getId();
    }

    /**
     * Xem kết quả quiz
     */
    @GetMapping("/quiz/result/{attemptId}")
    public String viewQuizResult(@PathVariable Long attemptId,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model) {

        User user = userService.findByEmail(userDetails.getUsername()); // Đổi tên biến
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kết quả"));

        // Kiểm tra quyền xem
        if (!attempt.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền xem kết quả này");
        }

        model.addAttribute("attempt", attempt);
        model.addAttribute("quiz", attempt.getQuiz());

        return "views/student/quiz-result";
    }

    /**
     * Xem lại chi tiết bài làm (câu nào đúng/sai)
     */
    @GetMapping("/quiz/review/{attemptId}")
    public String reviewQuiz(@PathVariable Long attemptId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {

        User user = userService.findByEmail(userDetails.getUsername()); // Đổi tên biến
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kết quả"));

        if (!attempt.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền xem kết quả này");
        }

        model.addAttribute("attempt", attempt);
        model.addAttribute("quiz", attempt.getQuiz());

        return "views/student/quiz-review";
    }

    /**
     * API cập nhật tiến độ bài học (cho AJAX)
     */
    @PostMapping("/api/lesson/{lessonId}/complete")
    @ResponseBody
    public String completeLesson(@PathVariable Long lessonId,
                                 @RequestParam Long enrollmentId,
                                 @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByEmail(userDetails.getUsername()); // Đổi tên biến
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy enrollment"));

        if (!enrollment.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Không có quyền");
        }

        // Cập nhật tiến độ (bạn cần implement method này)
        enrollmentService.updateProgress(enrollment, lessonId);

        return "OK";
    }

    /**
     * Tạo feedback dựa trên điểm số
     */
    private String generateFeedback(float score) {
        if (score >= 80) {
            return "Xuất sắc! Bạn đã nắm rất vững kiến thức của bài học này.";
        } else if (score >= 60) {
            return "Khá tốt! Bạn đã hiểu bài, nhưng cần ôn tập thêm một chút.";
        } else if (score >= 40) {
            return "Tạm ổn. Hãy xem lại video bài học và thử lại nhé!";
        } else {
            return "Cần cố gắng hơn. Đừng nản, hãy học lại bài học và thử quiz một lần nữa!";
        }
    }
}