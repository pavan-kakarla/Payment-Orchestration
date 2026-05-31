package com.example.payment.model;

public class PaymentResponse {
    private String transactionId;
    private String status;
    private String message;

    public PaymentResponse() {
    }

    public PaymentResponse(String transactionId, String status, String message) {
        this.transactionId = transactionId;
        this.status = status;
        this.message = message;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "PaymentResponse{" +
               "transactionId='" + transactionId + '\'' +
               ", status='" + status + '\'' +
               ", message='" + message + '\'' +
               '}';
    }
}
