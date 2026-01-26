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

    // --- TRANG LOGIN ---
    @GetMapping("/login")
    public String loginPage(Model model, HttpSession session) {
        String lastEmail = (String) session.getAttribute("LAST_EMAIL");
        if (lastEmail != null) {
            model.addAttribute("lastEmail", lastEmail);
            session.removeAttribute("LAST_EMAIL");
        }
        return "auth/login";
    }

    // --- TRANG CHỦ ---
//    @GetMapping("/")
//    public String home() {
//        return "auth/test";
//    }

    // --- ADMIN: TẮT DÒNG NÀY ĐỂ KHÔNG BỊ LỖI XUNG ĐỘT (VÌ ĐÃ CÓ AdminController) ---
    // @GetMapping("/admin/dashboard")
    // public String adminDashboard() {
    //     return "views/admin/dashboard";
    // }

    // --- TEACHER: GIỮ NGUYÊN DÒNG NÀY (ĐỂ KHÔNG BỊ LỖI 404) ---
//    @GetMapping("/teacher/dashboard")
//    public String teacherDashboard() {
//        return "teacher/dashboard";
//    }

    // --- STUDENT: TẮT DÒNG NÀY (VÌ ĐÃ CÓ StudentController) ---
    // @GetMapping("/student/dashboard")
    // public String studentDashboard() {
    //    return "views/student/dashboard";
    // }


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