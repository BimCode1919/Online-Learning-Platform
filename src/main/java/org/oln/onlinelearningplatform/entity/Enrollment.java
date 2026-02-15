package org.oln.onlinelearningplatform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments")
@Getter
@Setter
@NoArgsConstructor
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private String paymentStatus = "PENDING";

    private Double amount;

    private Double totalAmount;      // Tổng tiền HS trả (100$)
    private Double adminCommission;  // Tiền sàn thu (5$)
    private Double instructorShare;  // Tiền giảng viên nhận (95$)

    @Column(name = "vnp_txn_ref")
    private String vnpTxnRef;      // Mã giao dịch để đối soát với VNPay

    private LocalDateTime enrolledAt = LocalDateTime.now();
    private Double progressPercentage = 0.0;

    private Boolean isCompleted = false;
}
