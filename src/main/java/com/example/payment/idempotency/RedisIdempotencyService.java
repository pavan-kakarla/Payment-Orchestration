package com.example.payment.idempotency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class RedisIdempotencyService {
    private final StringRedisTemplate redis;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final Duration TTL = Duration.ofMinutes(5);
    private static final String PREFIX = "idem:";

    public RedisIdempotencyService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public String getRaw(String key) {
        return redis.opsForValue().get(PREFIX + key);
    }

    public boolean tryCreateInProgress(String key, String paymentId) {
        Map<String, Object> m = new HashMap<>();
        m.put("paymentId", paymentId);
        m.put("final", false);
        m.put("status", "PROCESSING");
        m.put("createdAt", Instant.now().toString());
        try {
            Boolean ok = redis.opsForValue().setIfAbsent(PREFIX + key, mapper.writeValueAsString(m), TTL);
            return Boolean.TRUE.equals(ok);
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    public void complete(String key, Map<String, Object> response) {
        try {
            Map<String, Object> out = new HashMap<>(response);
            out.put("final", true);
            out.put("completedAt", Instant.now().toString());
            redis.opsForValue().set(PREFIX + key, mapper.writeValueAsString(out), TTL);
        } catch (JsonProcessingException e) {
            // ignore
        }
    }
}

