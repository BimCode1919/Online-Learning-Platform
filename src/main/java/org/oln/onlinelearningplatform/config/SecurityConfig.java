package org.oln.onlinelearningplatform.config;

import org.oln.onlinelearningplatform.security.CustomAuthenticationFailureHandler;
import org.oln.onlinelearningplatform.security.CustomUserDetailsService; // Sẽ tạo ở bước 2
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll() // Cho phép file tĩnh
                        .requestMatchers("/login", "/register").permitAll() // Cho phép truy cập trang login/register
                        .requestMatchers("/instructor/**").hasRole("INSTRUCTOR") // Chỉ Instructor mới vào đc
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/student/**").hasAnyRole("STUDENT", "INSTRUCTOR", "ADMIN")
                        .anyRequest().authenticated() // Còn lại phải đăng nhập
                )
                .formLogin(form -> form
                        .loginPage("/login") // URL dẫn đến Controller login
                        .loginProcessingUrl("/perform_login") // URL Spring Security tự xử lý khi submit form
                        .usernameParameter("email")
                        .defaultSuccessUrl("/", true) // Đăng nhập xong về trang chủ
                        //.failureUrl("/login?error=true") // Thất bại thì về lại login kèm lỗi
                        .failureHandler(customAuthenticationFailureHandler) // cho phép tùy chỉnh xử lý lỗi, nghĩa là nhập sai password sẽ lưu email đã nhập vào session
                                                                            //cho phép giữ lại email đã nhập trong form
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Mã hóa mật khẩu
    }
}