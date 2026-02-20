package org.oln.onlinelearningplatform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "quizzes")
@Getter
@Setter
@NoArgsConstructor
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Enumerated(EnumType.STRING)
    private QuizType type; // MANUAL, AI

    @Enumerated(EnumType.STRING)
    private QuizStatus status; // GENERATING, READY, FAILED

    private String difficulty;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @OneToOne
    @JoinColumn(name = "lesson_id")
    private Lesson lesson; // nếu là AI quiz theo lesson

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL)
    private Set<Question> questions = new HashSet<>();

    public enum QuizType {
        MANUAL,
        AI
    }
    public enum QuizStatus {
        GENERATING,
        READY,
        FAILED
    }

}

