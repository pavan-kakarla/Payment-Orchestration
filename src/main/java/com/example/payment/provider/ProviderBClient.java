package com.example.payment.provider;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Component("providerB")
public class ProviderBClient implements PaymentProvider {
    private final Random random = new Random();

    @Override
    public ProviderResponse charge(Map<String, Object> request) {
        sleep(randomInt(60, 600));
        double r = random.nextDouble();
        if (r < 0.15) return ProviderResponse.timeout();
        if (r < 0.35) return ProviderResponse.failure(503, "service unavailable", true);
        if (r < 0.45) return ProviderResponse.failure(402, "payment required", false);
        Map<String, Object> body = new HashMap<>();
        body.put("provider", "B");
        body.put("status", "ok");
        body.put("tx", randomInt(200000, 999999));
        return ProviderResponse.success(200, body);
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    private int randomInt(int min, int max) { return random.nextInt(max - min + 1) + min; }
}

