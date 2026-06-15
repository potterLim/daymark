package com.potterlim.daymark.identity;

public final class WorkspaceId {

    private static final int MAX_LENGTH = 100;

    private final String mValue;

    private WorkspaceId(String value) {
        mValue = value;
    }

    public static WorkspaceId create(String valueOrNull) {
        String normalizedValue = normalize(valueOrNull);
        if (normalizedValue == null) {
            throw new IllegalArgumentException("workspaceId must not be blank.");
        }

        if (normalizedValue.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("workspaceId length must not exceed 100.");
        }

        return new WorkspaceId(normalizedValue);
    }

    public static WorkspaceId createOrNull(String valueOrNull) {
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
