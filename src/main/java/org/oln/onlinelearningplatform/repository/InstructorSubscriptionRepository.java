package org.oln.onlinelearningplatform.repository;

import org.oln.onlinelearningplatform.entity.InstructorSubscription;
import org.oln.onlinelearningplatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InstructorSubscriptionRepository extends JpaRepository<InstructorSubscription, Long> {
    Optional<InstructorSubscription> findFirstByUserAndStatusAndEndDateAfterOrderByEndDateDesc(
            User user, String status, LocalDateTime now);
    
    List<InstructorSubscription> findByUserOrderByEndDateDesc(User user);

    Optional<InstructorSubscription> findByVnpTxnRef(String vnpTxnRef);
}
