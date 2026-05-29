package com.example.payment.service;

import com.example.payment.idempotency.RedisIdempotencyService;
import com.example.payment.model.Payment;
import com.example.payment.model.ProviderAttempt;
import com.example.payment.provider.PaymentProvider;
import com.example.payment.provider.ProviderResponse;
import com.example.payment.repository.PaymentRepository;
import com.example.payment.repository.ProviderAttemptRepository;
import com.example.payment.routing.RoutingEngine;
import com.example.payment.service.dto.CreatePaymentRequest;
import com.example.payment.service.dto.CreatePaymentResponse;
import com.example.payment.service.dto.FetchPaymentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import javax.transaction.Transactional;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ProviderAttemptRepository attemptRepository;
    // idempotency now backed by Redis
    private final RedisIdempotencyService idempotencyService;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
    private final RoutingEngine routingEngine;
    private final Map<String, PaymentProvider> providers;

    private static final int MAX_RETRIES_PER_PROVIDER = 3;
    private static final long BASE_DELAY_MS = 200L;

    public PaymentService(PaymentRepository paymentRepository, ProviderAttemptRepository attemptRepository, RedisIdempotencyService idempotencyService, RoutingEngine routingEngine, List<PaymentProvider> providerList) {
        this.paymentRepository = paymentRepository;
        this.attemptRepository = attemptRepository;
        this.idempotencyService = idempotencyService;
        this.routingEngine = routingEngine;
        this.providers = new HashMap<>();
        for (PaymentProvider p : providerList) {
            // component name qualifies the provider bean name, but we will rely on class simple name mapping in routing
            this.providers.put(p.getClass().getAnnotation(org.springframework.stereotype.Component.class).value(), p);
        }
    }

    /**
     * Generates a unique fingerprint for the payment request to ensure idempotency.
     * This covers the requirement: "same payment if we receive in last 5 minutes".
     */
    private String generateRequestFingerprint(CreatePaymentRequest req) {
        try {
            String raw = String.format("%s:%d:%s:%s",
                    req.getMerchantReference(),
                    req.getAmount(),
                    req.getCurrency(),
                    req.getPaymentMethod());
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return "idem_hash_" + hexString.toString();
        } catch (Exception e) {
            return "idem_ref_" + req.getMerchantReference();
        }
    }

    @Transactional
    public CreatePaymentResponse createPayment(CreatePaymentRequest req) {
        // Generate idempotency key from the request body to detect "same payment"
        String effectiveKey = generateRequestFingerprint(req);

        // Check Redis store for final or in-progress entry
        String raw = idempotencyService.getRaw(effectiveKey);
        if (raw != null) {
            try {
                com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(raw);
                if (node.has("final") && node.get("final").asBoolean()) {
                    return new CreatePaymentResponse(
                            200,
                            node.get("paymentId").asText(),
                            node.get("status").asText(),
                            node.has("provider") ? node.get("provider").asText() : null
                    );
                } else {
                    // in-progress
                    return new CreatePaymentResponse(org.springframework.http.HttpStatus.ACCEPTED.value(), null, "PROCESSING", null);
                }
            } catch (Exception e) {
                // fallthrough and create new
            }
        }

        // create payment
        Payment p = new Payment();
        String paymentId = UUID.randomUUID().toString();
        p.setPaymentId(paymentId);
        p.setAmount(req.getAmount() != null ? req.getAmount() : 0L);
        p.setCurrency(req.getCurrency() != null ? req.getCurrency() : "INR");
        p.setPaymentMethod(req.getPaymentMethod() != null ? req.getPaymentMethod() : "CARD");
        p.setMerchantReference(req.getMerchantReference());
        p.setStatus("PROCESSING");
        p.setCreatedAt(Instant.now());
        p.setUpdatedAt(Instant.now());
        p.setAttempts(0);

        paymentRepository.save(p);

        // Register the key in Redis to lock the request for 5 minutes (TTL managed by idempotencyService)
        idempotencyService.tryCreateInProgress(effectiveKey, paymentId);

        // async orchestration
        orchestrate(paymentId, req, effectiveKey);

        return new CreatePaymentResponse(HttpStatus.CREATED.value(), paymentId, "PROCESSING", null);
    }

    @Async
    public void orchestrate(String paymentId, CreatePaymentRequest req, String idempotencyKey) {
        List<String> providerNames = routingEngine.getProvidersFor(req.getPaymentMethod());
        int attempts = 0;

        // FAILOVER STRATEGY: Iterate through available providers
        for (String pName : providerNames) {
            PaymentProvider provider = providers.get(pName);
            if (provider == null) continue;

            // RETRY STRATEGY: Up to MAX_RETRIES_PER_PROVIDER for the current provider
            for (int attemptNo = 1; attemptNo <= MAX_RETRIES_PER_PROVIDER; attemptNo++) {
                attempts++;
                Map<String, Object> mapReq = new HashMap<>();
                mapReq.put("paymentId", paymentId);
                mapReq.put("amount", req.getAmount());
                mapReq.put("currency", req.getCurrency());
                mapReq.put("paymentMethod", req.getPaymentMethod());

                ProviderResponse resp;
                try {
                    resp = provider.charge(mapReq);
                } catch (Exception ex) {
                    // Mark as retryable failure for network/unknown errors
                    resp = ProviderResponse.failure(0, ex.getMessage(), true);
                }

                ProviderAttempt att = new ProviderAttempt();
                att.setPaymentId(paymentId);
                att.setProvider(pName);
                att.setAttemptNo(attempts);
                att.setStatus(resp.isSuccess() ? "SUCCESS" : "FAILED");
                att.setResponse(resp.getBody().toString());
                att.setCreatedAt(Instant.now());
                attemptRepository.save(att);

                // update attempts count
                Optional<com.example.payment.model.Payment> opt = paymentRepository.findByPaymentId(paymentId);
                if (opt.isPresent()) {
                    Payment dbp = opt.get();
                    dbp.setAttempts(attempts);
                    dbp.setUpdatedAt(Instant.now());
                    paymentRepository.save(dbp);
                }

                if (resp.isSuccess()) {
                    // success
                    Optional<Payment> pOpt = paymentRepository.findByPaymentId(paymentId);
                    if (pOpt.isPresent()) {
                        Payment dbp = pOpt.get();
                        dbp.setStatus("SUCCESS");
                        dbp.setUpdatedAt(Instant.now());
                        paymentRepository.save(dbp);
                    }
                    java.util.Map<String, Object> out = new java.util.HashMap<>();
                    out.put("paymentId", paymentId);
                    out.put("status", "SUCCESS");
                    out.put("provider", pName);
                    idempotencyService.complete(idempotencyKey, out);
                    return;
                }

                // If not success and not retryable, break the retry loop and failover to next provider
                if (!resp.isRetryable()) break;

                try {
                    Thread.sleep(BASE_DELAY_MS * (1L << (attemptNo - 1)));
                } catch (InterruptedException ignored) {}
            }
        }

        // all exhausted
        Optional<Payment> pOpt = paymentRepository.findByPaymentId(paymentId);
        if (pOpt.isPresent()) {
            Payment dbp = pOpt.get();
            dbp.setStatus("FAILED");
            dbp.setUpdatedAt(Instant.now());
            paymentRepository.save(dbp);
        }

        java.util.Map<String, Object> out = new java.util.HashMap<>();
        out.put("paymentId", paymentId);
        out.put("status", "FAILED");
        idempotencyService.complete(idempotencyKey, out);
    }

    public FetchPaymentResponse fetchPayment(String paymentId) {
        Optional<Payment> p = paymentRepository.findByPaymentId(paymentId);
        if (!p.isPresent()) return null;
        List<ProviderAttempt> attempts = attemptRepository.findByPaymentIdOrderByIdAsc(paymentId);
        return new FetchPaymentResponse(p.get(), attempts);
    }
}