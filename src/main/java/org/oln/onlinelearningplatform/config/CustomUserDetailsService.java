package org.oln.onlinelearningplatform.config;

import org.oln.onlinelearningplatform.entity.User;
import org.oln.onlinelearningplatform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy email: " + email));

        // Logic xử lý thông minh: Nếu DB có ROLE_ rồi thì thôi, chưa có thì thêm vào
        String dbRole = user.getRole();
        String securityRole = dbRole.startsWith("ROLE_") ? dbRole : "ROLE_" + dbRole;

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(securityRole)
                .build();
    }
}
