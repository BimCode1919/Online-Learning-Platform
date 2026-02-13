package org.oln.onlinelearningplatform.controller;

import org.oln.onlinelearningplatform.entity.User;
import org.oln.onlinelearningplatform.service.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String showStudentLoginForm(Model model) {
        model.addAttribute("role", "STUDENT");
        return "auth/login";
    }

    @GetMapping("/instructor/login")
    public String showInstructorLoginForm(Model model) {
        model.addAttribute("role", "INSTRUCTOR");
        return "auth/login";
    }

    @GetMapping("/admin/login")
    public String showAdminLoginForm(Model model) {
        model.addAttribute("role", "ADMIN");
        return "auth/login";
    }

    @GetMapping("/register")
    public String showStudentRegister(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("role", "STUDENT");
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerStudent(@ModelAttribute("user") User user, RedirectAttributes ra) {
        if (authService.existsByEmail(user.getEmail())) {
            ra.addFlashAttribute("error", "Email đã tồn tại!");
            return "redirect:/register";
        }
        authService.registerUser(user, "STUDENT");
        ra.addFlashAttribute("success", "Đăng ký Học viên thành công!");
        return "redirect:/login";
    }

    // --- LUỒNG INSTRUCTOR ---
    @GetMapping("/instructor/register")
    public String showInstructorRegister(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("role", "INSTRUCTOR");
        return "auth/register";
    }

    @PostMapping("/instructor/register")
    public String registerInstructor(@ModelAttribute("user") User user, RedirectAttributes ra) {
        if (authService.existsByEmail(user.getEmail())) {
            ra.addFlashAttribute("error", "Email đã tồn tại!");
            return "redirect:/instructor/register";
        }
        authService.registerUser(user, "INSTRUCTOR");
        ra.addFlashAttribute("success", "Đăng ký Giảng viên thành công!");
        return "redirect:/instructor/login";
    }

    @GetMapping("/logout-success")
    public String logoutSuccess() {
        return "redirect:/login?logout";
    }
}
