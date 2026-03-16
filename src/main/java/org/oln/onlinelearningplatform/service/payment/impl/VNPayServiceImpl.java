package org.oln.onlinelearningplatform.service.payment.impl;

import jakarta.servlet.http.HttpServletRequest;

import org.oln.onlinelearningplatform.config.VNPayConfig;
import org.oln.onlinelearningplatform.entity.Enrollment;
import org.oln.onlinelearningplatform.entity.InstructorSubscription;
import org.oln.onlinelearningplatform.service.payment.VNPayService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VNPayServiceImpl implements VNPayService {

    @Value("${vnp_TmnCode}") private String tmnCode;
    @Value("${vnp_HashSecret}") private String hashSecret;
    @Value("${vnp_PayUrl}") private String payUrl;
    @Value("${vnp_ReturnUrl}") private String returnUrl;

    @Override
    public String createPaymentUrl(Enrollment enrollment, HttpServletRequest request) throws UnsupportedEncodingException {
        // Return URL for student
        String studentReturnUrl = returnUrl; 
        return buildUrl(enrollment.getVnpTxnRef(), enrollment.getTotalAmount(), "Thanh toan khoa hoc: " + enrollment.getVnpTxnRef(), studentReturnUrl);
    }

    @Override
    public String createPaymentUrl(InstructorSubscription subscription, HttpServletRequest request) throws UnsupportedEncodingException {
        // Return URL for instructor subscription
        String instructorReturnUrl = returnUrl.replace("/student/payment-callback", "/instructor/subscription/payment-callback");
        return buildUrl(subscription.getVnpTxnRef(), subscription.getAmount(), "Thanh toan goi Premium: " + subscription.getPlanType() + " - " + subscription.getVnpTxnRef(), instructorReturnUrl);
    }

    private String buildUrl(String txnRef, double rawAmount, String orderInfo, String customReturnUrl) throws UnsupportedEncodingException {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";

        long amount = (long) (rawAmount * 100);
        String vnp_IpAddr = "127.0.0.1";

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", tmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", txnRef);
        vnp_Params.put("vnp_OrderInfo", orderInfo);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", customReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext(); ) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayConfig.hmacSHA512(hashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        return payUrl + "?" + queryUrl;
    }
}