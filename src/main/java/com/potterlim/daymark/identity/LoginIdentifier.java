package com.potterlim.daymark.identity;

public final class LoginIdentifier {

    private final String mValue;

    private LoginIdentifier(String value) {
        mValue = value;
    }

    public static LoginIdentifier create(String valueOrNull) {
        String normalizedValue = normalize(valueOrNull);
        if (normalizedValue == null) {
            throw new IllegalArgumentException("loginIdentifier must not be blank.");
        }

        return new LoginIdentifier(normalizedValue);
    }

    public static LoginIdentifier createOrNull(String valueOrNull) {
        String normalizedValue = normalize(valueOrNull);
        if (normalizedValue == null) {
            return null;
        }

        return new LoginIdentifier(normalizedValue);
    }

    public String getValue() {
        return mValue;
    }

    private static String normalize(String valueOrNull) {
        if (valueOrNull == null || valueOrNull.isBlank()) {
            return null;
        }

        return valueOrNull.trim();
    }
}
