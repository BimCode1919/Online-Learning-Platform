package org.oln.onlinelearningplatform.repository;

import org.oln.onlinelearningplatform.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
}
