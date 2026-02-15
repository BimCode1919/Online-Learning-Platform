package org.oln.onlinelearningplatform.controller;

import org.oln.onlinelearningplatform.entity.Course;
import org.oln.onlinelearningplatform.entity.Enrollment;
import org.oln.onlinelearningplatform.entity.User;
import org.oln.onlinelearningplatform.service.auth.AuthService;
import org.oln.onlinelearningplatform.service.course.CourseService;
import org.oln.onlinelearningplatform.service.course.EnrollmentService;
import org.oln.onlinelearningplatform.service.user.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin") // Tất cả link bắt đầu bằng /admin
public class AdminController {

    private final UserService userService;
    private final AuthService authService; // Dùng để tạo user mới có mã hóa pass
    private final PasswordEncoder passwordEncoder;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;

    public AdminController(UserService userService, AuthService authService, PasswordEncoder passwordEncoder, CourseService courseService, EnrollmentService enrollmentService) {
        this.userService = userService;
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
    }

    // 1. Dashboard chính của Admin
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        // 1. Lấy danh sách chờ duyệt (PENDING)
        List<Course> pendingCourses = courseService.getCoursesByStatus("PENDING");
        model.addAttribute("pendingCount", pendingCourses.size());

        // 2. Đếm số lượng khóa học đã được duyệt (APPROVE)
        List<Course> approvedCourses = courseService.getCoursesByStatus("APPROVE");
        model.addAttribute("approvedCount", approvedCourses.size());

        // 3. Đếm tổng số người dùng trong hệ thống
        long userCount = userService.countAllUsers(); // Giả sử bạn có hàm này trong UserService
        model.addAttribute("userCount", userCount);

        // 4. Tính tổng doanh thu của hệ thống (5% Admin Commission)
        // Lấy tất cả Enrollment đã thanh toán thành công
        List<Enrollment> completedEnrollments = enrollmentService.getEnrollmentsByStatus("COMPLETED");

        double totalSystemRevenue = completedEnrollments.stream()
                .mapToDouble(e -> e.getAdminCommission() != null ? e.getAdminCommission() : 0.0)
                .sum();

        model.addAttribute("totalSystemRevenue", totalSystemRevenue);

        return "views/admin/dashboard";
    }

    @GetMapping("/courses/approval")
    public String showApprovalPage(Model model) {
        model.addAttribute("pendingCourses", courseService.getCoursesByStatus("PENDING"));
        return "views/admin/course-approval";
    }

    @PostMapping("/courses/approve/{id}")
    public String approveCourse(@PathVariable Long id) {
        courseService.updateCourseStatus(id, "APPROVE");
        return "redirect:/admin/courses/approval";
    }

    @GetMapping("/courses")
    public String listAllCourses(Model model) {
        // Lấy tất cả khóa học không phân biệt trạng thái
        model.addAttribute("allCourses", courseService.getAllCourses());
        return "views/admin/course-management";
    }

    @PostMapping("/courses/delete/{id}")
    public String deleteCourseByAdmin(@PathVariable Long id, RedirectAttributes ra) {
        try {
            courseService.deleteCourse(id); // Sử dụng hàm delete đã có
            ra.addFlashAttribute("success", "Đã xóa khóa học vĩnh viễn.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/courses";
    }

    @GetMapping("/course/{id}")
    public String viewCourseContent(@PathVariable Long id, Model model) {
        Course course = courseService.getCourseById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học với ID: " + id));
        model.addAttribute("course", course);
        return "views/admin/course-preview";
    }

    @PostMapping("/course/reject/{id}")
    public String rejectCourse(@PathVariable Long id, @RequestParam("note") String note) {
        Course course = courseService.getCourseById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học với ID: " + id));

        // 1. Lưu lời nhắn gửi đến giảng viên
        course.setNote(note);

        // 2. Cập nhật trạng thái thành FIX (Giảng viên cần sửa lại)
        course.setStatus("FIX");

        courseService.save(course); // Lưu thay đổi vào DB

        // 3. Quay về trang danh sách quản lý
        return "redirect:/admin/courses";
    }

    // 2. Trang Quản lý User (Danh sách)
    @GetMapping("/users")
    public String userManagement(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        return "views/admin/user-management"; // File html bạn đã có
    }

    @PostMapping("/users/create")
    public String createUser(@RequestParam String username,
                             @RequestParam String email,
                             @RequestParam String password,
                             @RequestParam String role,
                             RedirectAttributes ra) {
        try {
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setPassword(passwordEncoder.encode(password)); // Đừng quên mã hóa pass
            newUser.setRole(role);

            userService.saveUser(newUser);
            ra.addFlashAttribute("success", "Tạo người dùng thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // 5. Xóa User
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        return "redirect:/admin/users";
    }
}