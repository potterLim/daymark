package com.potterlim.daymark.security;

public final class RateLimitRequestLimit {

    private final int mValue;

    private RateLimitRequestLimit(int value) {
        mValue = value;
    }

    public static RateLimitRequestLimit of(int value) {
        if (value < 1) {
            throw new IllegalArgumentException("value must be positive.");
        }

        return new RateLimitRequestLimit(value);
    }

    public int getValue() {
        return mValue;
    }
}
