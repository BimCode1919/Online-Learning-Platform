package org.oln.onlinelearningplatform.service.payment;

import jakarta.servlet.http.HttpServletRequest;
import org.oln.onlinelearningplatform.entity.Enrollment;

import java.io.UnsupportedEncodingException;

public interface VNPayService {
    String createPaymentUrl(Enrollment enrollment, HttpServletRequest request) throws UnsupportedEncodingException;
    //int orderReturn(HttpServletRequest request);
}
