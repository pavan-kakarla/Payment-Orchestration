package com.example.payment.model;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "idempotency")
public class IdempotencyKey {
    @Id
    @Column(name = "idempotency_key")
    private String key;

    @Column(name = "payment_id")
    private String paymentId;

    @Column(name = "response_code")
    private Integer responseCode;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "created_at")
    private Instant createdAt;

    public IdempotencyKey() {}

    // getters/setters
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public Integer getResponseCode() { return responseCode; }
    public void setResponseCode(Integer responseCode) { this.responseCode = responseCode; }
    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

