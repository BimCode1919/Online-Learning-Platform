package org.oln.onlinelearningplatform.service.subscription.impl;

import org.oln.onlinelearningplatform.entity.InstructorSubscription;
import org.oln.onlinelearningplatform.entity.User;
import org.oln.onlinelearningplatform.repository.InstructorSubscriptionRepository;
import org.oln.onlinelearningplatform.service.subscription.InstructorSubscriptionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InstructorSubscriptionServiceImpl implements InstructorSubscriptionService {

    private final InstructorSubscriptionRepository repository;

    public InstructorSubscriptionServiceImpl(InstructorSubscriptionRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean hasActiveSubscription(User user) {
        return repository.findFirstByUserAndStatusAndEndDateAfterOrderByEndDateDesc(
                user, "ACTIVE", LocalDateTime.now()).isPresent();
    }

    @Override
    public InstructorSubscription subscribe(User user, String planType) {
        InstructorSubscription subscription = new InstructorSubscription();
        subscription.setUser(user);
        subscription.setPlanType(planType);
        subscription.setStartDate(LocalDateTime.now());
        subscription.setStatus("PENDING");
        subscription.setPaymentStatus("UNPAID");
        
        // Generate a transaction ref
        String txnRef = "SUB_" + System.currentTimeMillis() + "_" + user.getId();
        subscription.setVnpTxnRef(txnRef);

        LocalDateTime endDate;
        double amount;

        switch (planType.toUpperCase()) {
            case "MONTH":
                endDate = LocalDateTime.now().plusMonths(1);
                amount = 249000.0;
                break;
            case "QUARTER":
                endDate = LocalDateTime.now().plusMonths(3);
                amount = 599000.0;
                break;
            case "YEAR":
                endDate = LocalDateTime.now().plusYears(1);
                amount = 1999000.0;
                break;
            default:
                throw new IllegalArgumentException("Gói đăng ký không hợp lệ: " + planType);
        }

        subscription.setEndDate(endDate);
        subscription.setAmount(amount);

        return repository.save(subscription);
    }

    @Override
    public List<InstructorSubscription> getSubscriptionHistory(User user) {
        return repository.findByUserOrderByEndDateDesc(user);
    }

    @Override
    public InstructorSubscription getActiveSubscription(User user) {
        return repository.findFirstByUserAndStatusAndEndDateAfterOrderByEndDateDesc(
                user, "ACTIVE", LocalDateTime.now()).orElse(null);
    }

    @Override
    public void completeSubscription(String txnRef, String responseCode) {
        InstructorSubscription sub = repository.findByVnpTxnRef(txnRef).orElse(null);
        if (sub != null) {
            if ("00".equals(responseCode)) {
                sub.setStatus("ACTIVE");
                sub.setPaymentStatus("PAID");
            } else {
                sub.setStatus("FAILED");
                sub.setPaymentStatus("FAILED");
            }
            repository.save(sub);
        }
    }
}
