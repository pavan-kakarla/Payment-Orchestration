package com.example.payment.service.dto;

import com.example.payment.model.Payment;
import com.example.payment.model.ProviderAttempt;

import java.util.List;

public class FetchPaymentResponse {
    private Payment payment;
    private List<ProviderAttempt> attempts;

    public FetchPaymentResponse() {}
    public FetchPaymentResponse(Payment payment, List<ProviderAttempt> attempts) { this.payment = payment; this.attempts = attempts; }
    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }
    public List<ProviderAttempt> getAttempts() { return attempts; }
    public void setAttempts(List<ProviderAttempt> attempts) { this.attempts = attempts; }
}

