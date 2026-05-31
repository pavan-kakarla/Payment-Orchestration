package com.example.payment.routing;

import com.example.payment.model.PaymentResponse;
import org.springframework.stereotype.Component;

@Component("paymentFailureHandler")
public class PaymentFailureHandler {

    public void handleFailure(PaymentResponse response) {
        System.err.println("PaymentFailureHandler: Failed to process payment with transaction ID: " + response.getTransactionId() + ". Reason: " + response.getMessage());
        // Here you would typically log the error, trigger alerts, initiate compensation logic, etc.
    }
}
