package org.oln.onlinelearningplatform.repository;

import org.oln.onlinelearningplatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
