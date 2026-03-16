package org.oln.onlinelearningplatform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "instructor_subscriptions")
@Getter
@Setter
@NoArgsConstructor
public class InstructorSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String planType; // MONTH, QUARTER, YEAR

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Column(nullable = false)
    private String status = "PENDING"; // ACTIVE, EXPIRED, PENDING

    private Double amount;

    private String vnpTxnRef;
    private String paymentStatus; // PAID, UNPAID, FAILED
}
