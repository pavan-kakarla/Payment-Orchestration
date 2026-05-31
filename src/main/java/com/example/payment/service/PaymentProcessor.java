package com.example.payment.service;

import com.example.payment.model.PaymentRequest;
import com.example.payment.model.PaymentResponse;
import org.springframework.stereotype.Component;

@Component("paymentProcessor")
public class PaymentProcessor {

    public PaymentResponse process(PaymentRequest request) {
        System.out.println("Processing payment for: " + request.getAmount() + " to " + request.getRecipient() + " (Transaction ID: " + request.getTransactionId() + ")");

        // Simulate payment processing logic
        if (Math.random() > 0.3) { // 70% success rate
            return new PaymentResponse(request.getTransactionId(), "SUCCESS", "Payment processed successfully.");
        } else {
            // Simulate a business-level failure, not an exception that triggers retry
            System.err.println("Simulating payment failure for transaction: " + request.getTransactionId());
            return new PaymentResponse(request.getTransactionId(), "FAILED", "Payment service reported a failure.");
        }
    }
}
