package org.oln.onlinelearningplatform.repository;

import org.oln.onlinelearningplatform.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
}
