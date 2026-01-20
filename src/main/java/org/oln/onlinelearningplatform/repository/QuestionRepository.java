package org.oln.onlinelearningplatform.repository;

import org.oln.onlinelearningplatform.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}
