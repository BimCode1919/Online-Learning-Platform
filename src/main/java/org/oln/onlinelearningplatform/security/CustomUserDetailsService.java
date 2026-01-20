package org.oln.onlinelearningplatform.security;

import org.oln.onlinelearningplatform.entity.User;
import org.oln.onlinelearningplatform.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    // Lưu ý: Tham số vẫn tên là "username" do override từ Interface,
    // nhưng thực chất giá trị truyền vào sẽ là EMAIL.
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // SỬA: Dùng findByEmail
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy email: " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(), // Dùng Email làm định danh chính
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }
}