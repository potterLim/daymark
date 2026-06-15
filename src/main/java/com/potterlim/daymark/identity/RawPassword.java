package com.potterlim.daymark.identity;

public final class RawPassword {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 72;

    private final String mValue;

    private RawPassword(String value) {
        mValue = value;
    }

    public static RawPassword create(String valueOrNull) {
        if (valueOrNull == null || valueOrNull.isBlank()) {
            throw new IllegalArgumentException("rawPassword must not be blank.");
        }

        int rawPasswordLength = valueOrNull.length();
        if (rawPasswordLength < MIN_LENGTH || rawPasswordLength > MAX_LENGTH) {
            throw new IllegalArgumentException("rawPassword length must be between 8 and 72.");
        }

        return new RawPassword(valueOrNull);
    }

    public String getValue() {
        return mValue;
    }
}
