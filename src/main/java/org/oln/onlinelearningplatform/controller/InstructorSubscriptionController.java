package org.oln.onlinelearningplatform.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.oln.onlinelearningplatform.entity.InstructorSubscription;
import org.oln.onlinelearningplatform.entity.User;
import org.oln.onlinelearningplatform.service.payment.VNPayService;
import org.oln.onlinelearningplatform.service.subscription.InstructorSubscriptionService;
import org.oln.onlinelearningplatform.service.user.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/instructor/subscription")
public class InstructorSubscriptionController {

    private final InstructorSubscriptionService subscriptionService;
    private final UserService userService;
    private final VNPayService vnpayService;

    public InstructorSubscriptionController(InstructorSubscriptionService subscriptionService, UserService userService, VNPayService vnpayService) {
        this.subscriptionService = subscriptionService;
        this.userService = userService;
        this.vnpayService = vnpayService;
    }

    @GetMapping("/pricing")
    public String showPricing(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User instructor = userService.findByEmail(userDetails.getUsername());
        InstructorSubscription activeSub = subscriptionService.getActiveSubscription(instructor);
        
        model.addAttribute("activeSubscription", activeSub);
        model.addAttribute("history", subscriptionService.getSubscriptionHistory(instructor));
        return "views/teacher/pricing";
    }

    @PostMapping("/subscribe")
    public String subscribe(@RequestParam("planType") String planType,
                            @AuthenticationPrincipal UserDetails userDetails,
                            HttpServletRequest request,
                            RedirectAttributes redirectAttributes) {
        User instructor = userService.findByEmail(userDetails.getUsername());
        
        try {
            InstructorSubscription sub = subscriptionService.subscribe(instructor, planType);
            String paymentUrl = vnpayService.createPaymentUrl(sub, request);
            return "redirect:" + paymentUrl;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lối: " + e.getMessage());
            return "redirect:/instructor/subscription/pricing";
        }
    }

    @GetMapping("/payment-callback")
    public String paymentCallback(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
        String vnp_TxnRef = request.getParameter("vnp_TxnRef");

        try {
            subscriptionService.completeSubscription(vnp_TxnRef, vnp_ResponseCode);
            if ("00".equals(vnp_ResponseCode)) {
                redirectAttributes.addFlashAttribute("success", "Đăng ký Premium thành công! Bây giờ bạn đã có thể dùng AI Quiz.");
                return "redirect:/instructor/dashboard";
            } else {
                redirectAttributes.addFlashAttribute("error", "Thanh toán không thành công. Vui lòng thử lại!");
                return "redirect:/instructor/subscription/pricing";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/instructor/subscription/pricing";
        }
    }
}
