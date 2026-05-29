package com.example.payment.provider;

import java.util.Map;

public interface PaymentProvider {
    ProviderResponse charge(Map<String, Object> request);
}

