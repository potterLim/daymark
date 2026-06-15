package com.potterlim.daymark.identity;

public final class PasswordHash {

    private final String mValue;

    private PasswordHash(String value) {
        mValue = value;
    }

    public static PasswordHash create(String valueOrNull) {
        if (valueOrNull == null || valueOrNull.isBlank()) {
            throw new IllegalArgumentException("passwordHash must not be blank.");
        }

        return new PasswordHash(valueOrNull);
    }

    public String getValue() {
        return mValue;
    }
}
