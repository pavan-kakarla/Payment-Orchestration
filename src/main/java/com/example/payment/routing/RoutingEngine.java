package com.example.payment.routing;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class RoutingEngine {
    // Simple static routing: CARD -> ProviderA, UPI -> ProviderB
    public List<String> getProvidersFor(String paymentMethod) {
        if (paymentMethod == null) return Arrays.asList("providerA", "providerB");
        String pm = paymentMethod.toUpperCase();
        if (pm.equals("CARD")) return Arrays.asList("providerA", "providerB");
        if (pm.equals("UPI")) return Arrays.asList("providerB", "providerA");
        return Arrays.asList("providerA", "providerB");
    }
}

