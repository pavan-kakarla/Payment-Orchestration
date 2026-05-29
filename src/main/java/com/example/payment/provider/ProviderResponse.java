package com.example.payment.provider;

import java.util.Collections;
import java.util.Map;

public class ProviderResponse {
    private final boolean success;
    private final int code;
    private final Map<String, Object> body;
    private final boolean retryable;

    private ProviderResponse(boolean success, int code, Map<String, Object> body, boolean retryable) {
        this.success = success;
        this.code = code;
        this.body = body == null ? Collections.emptyMap() : body;
        this.retryable = retryable;
    }

    public static ProviderResponse success(int code, Map<String, Object> body) { return new ProviderResponse(true, code, body, false); }
    public static ProviderResponse failure(int code, String message, boolean retryable) {
        return new ProviderResponse(false, code, Collections.singletonMap("message", message), retryable);
    }
    public static ProviderResponse timeout() { return new ProviderResponse(false, 0, Collections.singletonMap("message", "timeout"), true); }

    public boolean isSuccess() { return success; }
    public int getCode() { return code; }
    public Map<String, Object> getBody() { return body; }
    public boolean isRetryable() { return retryable; }
}

