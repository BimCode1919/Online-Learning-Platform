package org.oln.onlinelearningplatform.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Set;

@Configuration
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // 1. Lấy danh sách quyền (Role) của người vừa đăng nhập
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

        // 2. Kiểm tra Role và điều hướng về Dashboard tương ứng
        if (roles.contains("ROLE_ADMIN")) {
            response.sendRedirect("/admin/dashboard");
        } else if (roles.contains("ROLE_INSTRUCTOR")) {
            response.sendRedirect("/teacher/dashboard");
        } else if (roles.contains("ROLE_STUDENT")) {
            response.sendRedirect("/student/dashboard");
        } else {
            // Trường hợp không có role nào (ít gặp), đá về trang chủ chung
            response.sendRedirect("/");
        }
    }
}