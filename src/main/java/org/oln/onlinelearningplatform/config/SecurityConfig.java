package org.oln.onlinelearningplatform.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomAuthenticationSuccessHandler successHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Tắt CSRF để thuận tiện cho việc test POST form trong giai đoạn phát triển
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // Cho phép tất cả mọi người truy cập tài nguyên tĩnh và các trang công khai
                        .requestMatchers("/", "/login", "/register", "/instructor/login", "/instructor/register", "/admin/login").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()

                        // Phân quyền Dashboard
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/instructor/**").hasRole("INSTRUCTOR")
                        .requestMatchers("/student/**").hasRole("STUDENT")

                        // Tất cả các yêu cầu khác phải xác thực
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login") // Đảm bảo URL này khớp với @GetMapping("/login") trong Controller
                        .loginProcessingUrl("/perform_login")
                        .usernameParameter("email")
                        .successHandler(successHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/?logout") // Logout xong về trang chủ index
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
