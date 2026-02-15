package org.oln.onlinelearningplatform.controller;


import jakarta.servlet.http.HttpServletRequest;
import org.oln.onlinelearningplatform.entity.*;
import org.oln.onlinelearningplatform.repository.EnrollmentRepository;
import org.oln.onlinelearningplatform.service.course.CourseService;
import org.oln.onlinelearningplatform.service.course.EnrollmentService;
import org.oln.onlinelearningplatform.service.payment.VNPayService;
import org.oln.onlinelearningplatform.service.user.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/student")

public class StudentController {

    private final CourseService courseService;
    private final UserService userService;
    private final EnrollmentService enrollmentService;
    private final VNPayService vnPayService;
    private final EnrollmentRepository enrollmentRepository;

    public StudentController(CourseService courseService, UserService userService, EnrollmentService enrollmentService, VNPayService vnPayService, EnrollmentRepository enrollmentRepository) {
        this.courseService = courseService;
        this.userService = userService;
        this.enrollmentService = enrollmentService;
        this.vnPayService = vnPayService;
        this.enrollmentRepository = enrollmentRepository;
    }

    @GetMapping("/dashboard")
    public String viewDashboard(Model model,
                                @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        User userOpt = userService.findByEmail(email);

        model.addAttribute("currentUser", userOpt);

        // Lấy dashboard stats
        DashboardStatsDTO stats = courseService.getDashboardStats(userOpt.getId());
        model.addAttribute("stats", stats);

        return "views/student/dashboard";
    }

    @GetMapping("/courses")
    public String listAvailableCourses(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // 1. Lấy thông tin user hiện tại (để hiển thị avatar/tên nếu cần)
        String email = userDetails.getUsername();
        User user = userService.findByEmail(email);
        model.addAttribute("currentUser", user);

        // 2. Lấy danh sách các khóa học đã được DUYỆT (APPROVE)
        // Giả sử bạn có phương thức này trong Repository hoặc Service
        List<Course> availableCourses = courseService.getCoursesByStatus("APPROVE");
        model.addAttribute("availableCourses", availableCourses);

        return "views/student/courses";
    }

    @GetMapping("/course/detail/{id}")
    public String viewCourseDetail(@PathVariable Long id,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   Model model) {
        // 1. Lấy thông tin User hiện tại
        String email = userDetails.getUsername();
        User user = userService.findByEmail(email);

        // 2. Lấy thông tin khóa học (Eager load Lessons để hiển thị danh sách bài học)
        Course course = courseService.getCourseById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học với ID: " + id));

        // 3. Tìm thông tin Enrollment của user với khóa học này (nếu có)
        Enrollment enrollment = enrollmentService.findByUserAndCourse(user.getId(), id);

        model.addAttribute("course", course);
        model.addAttribute("enrollment", enrollment); // Có thể null nếu chưa đăng ký
        model.addAttribute("currentUser", user);

        return "views/student/course-detail";
    }

    @PostMapping("/course/enroll/{id}")
    public String handleEnrollment(@PathVariable Long id,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   HttpServletRequest request) {
        // 1. Lấy thông tin User và Course
        User user = userService.findByEmail(userDetails.getUsername());
        Course course = courseService.getCourseById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thầy khóa học với ID: " + id));

        // 2. Kiểm tra nếu đã có Enrollment rồi (Tránh thanh toán 2 lần)
        Enrollment existing = enrollmentService.findByUserAndCourse(user.getId(), course.getId());
        if (existing != null && "COMPLETED".equals(existing.getPaymentStatus())) {
            return "redirect:/student/learning/" + id;
        }

        // 3. Khởi tạo bản ghi Enrollment với trạng thái PENDING
        Enrollment enrollment = (existing != null) ? existing : new Enrollment();
        enrollment.setUser(user);
        enrollment.setCourse(course);

        // Tính toán phân chia tiền (Revenue Share)
        double price = course.getPrice();
        enrollment.setTotalAmount(price);
        enrollment.setAdminCommission(price * 0.05); // 5% cho sàn
        enrollment.setInstructorShare(price * 0.95); // 95% cho giảng viên

        enrollment.setPaymentStatus("PENDING");
        // Tạo mã giao dịch duy nhất (Transaction Reference)
        enrollment.setVnpTxnRef("DEV" + String.valueOf(System.currentTimeMillis()));

        enrollmentService.save(enrollment);

        // 4. Gọi Service tạo URL VNPay và Redirect
        try {
            String paymentUrl = vnPayService.createPaymentUrl(enrollment, request);
            return "redirect:" + paymentUrl;
        } catch (UnsupportedEncodingException e) {
            return "redirect:/student/course/detail/" + id + "?error=payment_failed";
        }
    }

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
                // Người dùng hủy hoặc lỗi: Xóa bản ghi PENDING để họ có thể nhấn "Enroll" lại từ đầu
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

    @GetMapping("/my-courses")
    public String showMyCourses(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername());

        // Lấy danh sách Enrollment có trạng thái COMPLETED
        List<Enrollment> myEnrollments = enrollmentService.findByUserIdAndPaymentStatus(user.getId(), "COMPLETED");

        model.addAttribute("enrollments", myEnrollments);
        return "views/student/my-courses-list";
    }

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

        model.addAttribute("course", course);
        model.addAttribute("enrollment", enrollment);

        return "views/student/learning-dashboard";
    }
}