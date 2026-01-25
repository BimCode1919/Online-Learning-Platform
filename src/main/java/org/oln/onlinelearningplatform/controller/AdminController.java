package org.oln.onlinelearningplatform.controller;

import org.oln.onlinelearningplatform.entity.User;
import org.oln.onlinelearningplatform.service.auth.AuthService;
import org.oln.onlinelearningplatform.service.user.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin") // Tất cả link bắt đầu bằng /admin
public class AdminController {

    private final UserService userService;
    private final AuthService authService; // Dùng để tạo user mới có mã hóa pass
    private final PasswordEncoder passwordEncoder;

    public AdminController(UserService userService, AuthService authService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. Dashboard chính của Admin
    @GetMapping("/dashboard")
    public String dashboard() {
        return "views/admin/dashboard";
    }

    // 2. Trang Quản lý User (Danh sách)
    @GetMapping("/users")
    public String userManagement(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        return "views/admin/user-management"; // File html bạn đã có
    }

    // 3. Form thêm mới User (Admin tạo giúp Giảng viên/Học viên)
    @GetMapping("/users/create")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new User());
        return "views/admin/user-form"; // Tạo file này sau
    }

    // 4. Xử lý lưu User mới
    @PostMapping("/users/save")
    public String saveUser(@ModelAttribute("user") User user) {

        // TRƯỜNG HỢP 1: TẠO MỚI (Chưa có ID)
        if (user.getId() == null) {
            authService.registerUser(user); // Logic cũ: mã hóa pass và lưu
        }
        // TRƯỜNG HỢP 2: CẬP NHẬT (Đã có ID)
        else {
            User existingUser = userService.findById(user.getId()).get();

            // Logic xử lý mật khẩu thông minh:
            if (user.getPassword().isEmpty()) {
                // Nếu Admin bỏ trống -> Giữ nguyên mật khẩu cũ
                user.setPassword(existingUser.getPassword());
            } else {
                // Nếu Admin nhập gì đó -> Mã hóa mật khẩu mới
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }

            // Các trường khác cập nhật bình thường
            existingUser.setUsername(user.getUsername());
            existingUser.setEmail(user.getEmail());
            existingUser.setRole(user.getRole());
            existingUser.setPassword(user.getPassword());

            userService.saveUser(existingUser);
        }

        return "redirect:/admin/users";
    }

    // 5. Xóa User
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/users/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        User user = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));

        // Mẹo: Gửi password rỗng ra view để ô mật khẩu trống trơn (bảo mật)
        user.setPassword("");

        model.addAttribute("user", user);
        return "views/admin/user-form"; // Tái sử dụng form cũ
    }

    // --- 2. CẬP NHẬT HÀM SAVE: XỬ LÝ MẬT KHẨU ---

}