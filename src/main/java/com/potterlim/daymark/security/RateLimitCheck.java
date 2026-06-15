package com.potterlim.daymark.security;

import java.time.Duration;

public final class RateLimitCheck {

    private final RateLimitKey mKey;
    private final RateLimitRequestLimit mRequestLimit;
    private final Duration mWindowDuration;

    private RateLimitCheck(
        RateLimitKey key,
        RateLimitRequestLimit requestLimit,
        Duration windowDuration
    ) {
        mKey = key;
        mRequestLimit = requestLimit;
        mWindowDuration = windowDuration;
    }

    public static RateLimitCheck of(
        RateLimitKey key,
        RateLimitRequestLimit requestLimit,
        Duration windowDuration
    ) {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null.");
        }

        if (requestLimit == null) {
            throw new IllegalArgumentException("requestLimit must not be null.");
        }

        if (windowDuration == null || windowDuration.isZero() || windowDuration.isNegative()) {
            throw new IllegalArgumentException("windowDuration must be positive.");
        }

        return new RateLimitCheck(key, requestLimit, windowDuration);
    }

    public String getKey() {
        return mKey.getValue();
    }

    public int getRequestLimit() {
        return mRequestLimit.getValue();
    }

    public Duration getWindowDuration() {
        return mWindowDuration;
    }
}
