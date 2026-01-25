package org.oln.onlinelearningplatform.controller;

import jakarta.servlet.http.HttpSession;
import org.oln.onlinelearningplatform.entity.User;
import org.oln.onlinelearningplatform.service.auth.AuthService;
import org.oln.onlinelearningplatform.service.user.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    // --- TRANG LOGIN (Ai cũng vào được) ---
    @GetMapping("/login")
    public String loginPage(Model model, HttpSession session) {
        // Kiểm tra xem có email lưu tạm trong session không (do nhập sai lần trước)
        String lastEmail = (String) session.getAttribute("LAST_EMAIL");

        if (lastEmail != null) {
            // Đưa vào model để HTML hiển thị
            model.addAttribute("lastEmail", lastEmail);

            // Xóa ngay khỏi session để dọn dẹp
            session.removeAttribute("LAST_EMAIL");
        }

        return "auth/login";
    }
    // --- TRANG ĐÍCH SAU KHI LOGIN (Chính là trang Test của bạn) ---
    // Spring Security sẽ chặn người chưa login ở đây
    @GetMapping("/")
    public String home() {
        // Trả về file test.html nằm trong folder templates/auth/
        return "auth/test";
    }


//    @GetMapping("/admin/dashboard")
//    public String adminDashboard() {
//        return "views/admin/dashboard"; // Trỏ đúng file html trong folder views/admin
//    }

    @GetMapping("/teacher/dashboard")
    public String teacherDashboard() {
        return "views/teacher/dashboard"; // Trỏ đúng file html trong folder views/teacher
    }

    @GetMapping("/student/dashboard")
    public String studentDashboard() {
        return "views/student/dashboard"; // Trỏ đúng file html trong folder views/student
    }


    // --- REGISTER ---
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute("user") User user, Model model) {
        if (userService.existsByUsername(user.getUsername())) {
            model.addAttribute("error", "Username đã tồn tại!");
            return "auth/register";
        }
        authService.registerUser(user);
        return "redirect:/login?success";
    }
}