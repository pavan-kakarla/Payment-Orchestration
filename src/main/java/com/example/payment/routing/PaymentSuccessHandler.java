package com.example.payment.routing;

import com.example.payment.model.PaymentResponse;
import org.springframework.stereotype.Component;

@Component("paymentSuccessHandler")
public class PaymentSuccessHandler {

    public void handleSuccess(PaymentResponse response) {
        System.out.println("PaymentSuccessHandler: Successfully processed payment with transaction ID: " + response.getTransactionId());
        // Here you would typically update your database, send notifications, etc.
    }
}
