package org.oln.onlinelearningplatform.service.subscription;

import org.oln.onlinelearningplatform.entity.InstructorSubscription;
import org.oln.onlinelearningplatform.entity.User;

import java.util.List;

public interface InstructorSubscriptionService {
    boolean hasActiveSubscription(User user);
    InstructorSubscription subscribe(User user, String planType);
    List<InstructorSubscription> getSubscriptionHistory(User user);
    InstructorSubscription getActiveSubscription(User user);
    void completeSubscription(String txnRef, String responseCode);
}
