package org.oln.onlinelearningplatform.repository;

import org.oln.onlinelearningplatform.entity.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
}
