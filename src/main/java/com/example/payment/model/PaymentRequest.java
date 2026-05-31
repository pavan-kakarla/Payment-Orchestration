package com.example.payment.model;

public class PaymentRequest {
    private String transactionId;
    private double amount;
    private String currency;
    private String recipient;

    public PaymentRequest() {
    }

    public PaymentRequest(String transactionId, double amount, String currency, String recipient) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.currency = currency;
        this.recipient = recipient;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    @Override
    public String toString() {
        return "PaymentRequest{" +
               "transactionId='" + transactionId + '\'' +
               ", amount=" + amount +
               ", currency='" + currency + '\'' +
               ", recipient='" + recipient + '\'' +
               '}';
    }
}
