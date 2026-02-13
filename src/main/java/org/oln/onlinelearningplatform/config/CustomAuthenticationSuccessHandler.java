package org.oln.onlinelearningplatform.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        var authorities = authentication.getAuthorities();
        String redirectUrl = "/login?error"; // Mặc định nếu không xác định được quyền

        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();
            if (role.equals("ROLE_ADMIN")) {
                redirectUrl = "/admin/dashboard";
                break;
            } else if (role.equals("ROLE_INSTRUCTOR")) {
                redirectUrl = "/instructor/dashboard";
                break;
            } else if (role.equals("ROLE_STUDENT")) {
                redirectUrl = "/student/dashboard";
                break;
            }
        }
        response.sendRedirect(redirectUrl);
    }
}
