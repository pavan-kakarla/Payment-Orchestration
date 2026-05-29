package com.example.payment.service.dto;

public class CreatePaymentResponse {
    private int httpStatus;
    private String paymentId;
    private String status;
    private String provider;

    public CreatePaymentResponse() {}

    public CreatePaymentResponse(int httpStatus, String paymentId, String status, String provider) {
        this.httpStatus = httpStatus;
        this.paymentId = paymentId;
        this.status = status;
        this.provider = provider;
    }

    public int getHttpStatus() { return httpStatus; }
    public void setHttpStatus(int httpStatus) { this.httpStatus = httpStatus; }
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
}

