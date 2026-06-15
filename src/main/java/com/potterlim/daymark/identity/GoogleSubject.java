package com.potterlim.daymark.identity;

public final class GoogleSubject {

    private static final int MAX_LENGTH = 255;

    private final String mValue;

    private GoogleSubject(String value) {
        mValue = value;
    }

    public static GoogleSubject create(String valueOrNull) {
        String normalizedValue = normalize(valueOrNull);
        if (normalizedValue == null) {
            throw new IllegalArgumentException("googleSubject must not be blank.");
        }

        if (normalizedValue.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("googleSubject length must not exceed 255.");
        }

        return new GoogleSubject(normalizedValue);
    }

    public static GoogleSubject createOrNull(String valueOrNull) {
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

        return valueOrNull.trim();
    }
}
