package com.example.payment.provider;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Component("providerA")
public class ProviderAClient implements PaymentProvider {
    private final Random random = new Random();

    @Override
    public ProviderResponse charge(Map<String, Object> request) {
        // simulate latency
        sleep(randomInt(80, 400));
        double r = random.nextDouble();
        if (r < 0.12) return ProviderResponse.timeout();
        if (r < 0.3) return ProviderResponse.failure(502, "bad gateway", true);
        if (r < 0.4) return ProviderResponse.failure(400, "invalid request", false);
        Map<String, Object> body = new HashMap<>();
        body.put("provider", "A");
        body.put("status", "ok");
        body.put("tx", randomInt(100000, 999999));
        return ProviderResponse.success(200, body);
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    private int randomInt(int min, int max) { return random.nextInt(max - min + 1) + min; }
}

