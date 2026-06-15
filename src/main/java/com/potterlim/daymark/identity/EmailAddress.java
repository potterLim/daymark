package com.potterlim.daymark.identity;

import java.util.Locale;

public final class EmailAddress {

    private static final int MAX_LENGTH = 255;

    private final String mValue;

    private EmailAddress(String value) {
        mValue = value;
    }

    public static EmailAddress create(String valueOrNull) {
        String normalizedValue = normalize(valueOrNull);
        if (normalizedValue == null) {
            throw new IllegalArgumentException("emailAddress must not be blank.");
        }

        if (normalizedValue.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("emailAddress length must not exceed 255.");
        }

        return new EmailAddress(normalizedValue);
    }

    public static EmailAddress createOrNull(String valueOrNull) {
        String normalizedValue = normalize(valueOrNull);
        if (normalizedValue == null) {
            return null;
        }

        return create(normalizedValue);
    }

    public String getValue() {
        return mValue;
    }

    private static String normalize(String valueOrNull) {
        if (valueOrNull == null || valueOrNull.isBlank()) {
            return null;
        }

        return valueOrNull.trim().toLowerCase(Locale.ROOT);
    }
}
