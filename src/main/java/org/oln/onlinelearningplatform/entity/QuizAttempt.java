package org.oln.onlinelearningplatform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_attempts")
@Getter
@Setter
@NoArgsConstructor
public class QuizAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    private Float score; // Điểm số (0-100)

    private Integer totalQuestions; // Thêm trường này

    private Integer correctAnswers; // Thêm trường này

    @Column(columnDefinition = "nvarchar(max)")
    private String aiFeedback;

    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String answersJson; // Lưu đáp án dạng JSON

    // Thêm method tiện ích
    public String getScorePercentage() {
        if (score == null) return "0%";
        return Math.round(score) + "%";
    }

    public String getResultSummary() {
        if (correctAnswers == null || totalQuestions == null) return "";
        return correctAnswers + "/" + totalQuestions + " câu đúng";
    }
}