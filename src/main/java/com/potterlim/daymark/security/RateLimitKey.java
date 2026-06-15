package com.potterlim.daymark.security;

public final class RateLimitKey {

    private final String mValue;

    private RateLimitKey(String value) {
        mValue = value;
    }

    public static RateLimitKey create(String valueOrNull) {
        if (valueOrNull == null || valueOrNull.isBlank()) {
            throw new IllegalArgumentException("value must not be blank.");
        }

        return new RateLimitKey(valueOrNull);
    }

    public String getValue() {
        return mValue;
    }
}
