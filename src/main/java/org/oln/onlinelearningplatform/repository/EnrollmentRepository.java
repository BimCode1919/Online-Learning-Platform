package org.oln.onlinelearningplatform.repository;

import org.oln.onlinelearningplatform.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long userId1);
    Optional<Enrollment> findByVnpTxnRef(String vnp_TxnRef);

    List<Enrollment> findByUserIdAndPaymentStatus(Long userId, String paymentStatus);
    List<Enrollment> findByPaymentStatus(String status);



}
