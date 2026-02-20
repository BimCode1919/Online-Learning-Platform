// QuizAttemptServiceImpl.java
package org.oln.onlinelearningplatform.service.quiz.impl;

import org.oln.onlinelearningplatform.entity.QuizAttempt;
import org.oln.onlinelearningplatform.repository.QuizAttemptRepository;
import org.oln.onlinelearningplatform.service.quiz.QuizAttemptService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuizAttemptServiceImpl implements QuizAttemptService {

    private final QuizAttemptRepository quizAttemptRepository;

    public QuizAttemptServiceImpl(QuizAttemptRepository quizAttemptRepository) {
        this.quizAttemptRepository = quizAttemptRepository;
    }

    @Override
    public QuizAttempt save(QuizAttempt attempt) {
        return quizAttemptRepository.save(attempt);
    }

    @Override
    public List<QuizAttempt> findByUserAndCourse(Long userId, Long courseId) {
        return quizAttemptRepository.findByUserIdAndCourseId(userId, courseId);
    }

    @Override
    public Set<Long> findCompletedQuizIdsByUserAndCourse(Long userId, Long courseId) {
        List<Long> completedIds = quizAttemptRepository
                .findCompletedQuizIdsByStudentIdAndCourseId(userId, courseId);
        return Set.copyOf(completedIds);
    }
}