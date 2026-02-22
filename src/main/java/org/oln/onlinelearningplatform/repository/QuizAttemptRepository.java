package org.oln.onlinelearningplatform.repository;

import org.oln.onlinelearningplatform.entity.Quiz;
import org.oln.onlinelearningplatform.entity.QuizAttempt;
import org.oln.onlinelearningplatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    Optional<QuizAttempt> findByUserAndQuiz(User user, Quiz quiz);

    List<QuizAttempt> findByUserId(Long studentId);

    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.user.id = :studentId AND qa.quiz.lesson.course.id = :courseId")
    List<QuizAttempt> findByUserIdAndCourseId(@Param("studentId") Long studentId,
                                                 @Param("courseId") Long courseId);

    @Query("SELECT qa.quiz.id FROM QuizAttempt qa WHERE qa.user.id = :studentId AND qa.quiz.lesson.course.id = :courseId")
    List<Long> findCompletedQuizIdsByStudentIdAndCourseId(@Param("studentId") Long studentId,
                                                          @Param("courseId") Long courseId);
}