package org.oln.onlinelearningplatform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    private String password;
    private String email;
    private String role;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Mapping ngược để dễ truy xuất
    @OneToMany(mappedBy = "instructor")
    private List<Course> courses;

    @OneToMany(mappedBy = "user")
    private List<QuizAttempt> quizAttempts;
}