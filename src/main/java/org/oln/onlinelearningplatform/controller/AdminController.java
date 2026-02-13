package org.oln.onlinelearningplatform.controller;

import org.oln.onlinelearningplatform.entity.Course;
import org.oln.onlinelearningplatform.entity.User;
import org.oln.onlinelearningplatform.service.auth.AuthService;
import org.oln.onlinelearningplatform.service.course.CourseService;
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

    public AdminController(UserService userService, AuthService authService, PasswordEncoder passwordEncoder, CourseService courseService) {
        this.userService = userService;
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
        this.courseService = courseService;
    }

    // 1. Dashboard chính của Admin
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        // Lấy danh sách chờ duyệt để đếm số lượng
        List<Course> pendingCourses = courseService.getCoursesByStatus("PENDING");
        model.addAttribute("pendingCount", pendingCourses.size());
        return "views/admin/dashboard"; // đường dẫn file của bạn
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