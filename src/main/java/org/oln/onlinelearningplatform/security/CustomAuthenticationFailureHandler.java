package org.oln.onlinelearningplatform.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        // 1. Lấy email người dùng vừa nhập
        String email = request.getParameter("email");

        // 2. Lưu tạm vào Session với tên là "LAST_EMAIL"
        if (email != null) {
            request.getSession().setAttribute("LAST_EMAIL", email);
        }

        // 3. Chuyển hướng về trang login kèm thông báo lỗi (giống mặc định)
        setDefaultFailureUrl("/login?error=true");
        super.onAuthenticationFailure(request, response, exception);
    }
}