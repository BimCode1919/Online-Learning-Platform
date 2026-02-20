package org.oln.onlinelearningplatform.service.quiz;

import org.oln.onlinelearningplatform.entity.QuizAttempt;

import java.util.List;
import java.util.Set;

public interface QuizAttemptService {
    QuizAttempt save(QuizAttempt attempt);
    List<QuizAttempt> findByUserAndCourse(Long userId, Long courseId);
    Set<Long> findCompletedQuizIdsByUserAndCourse(Long userId, Long courseId);
}
