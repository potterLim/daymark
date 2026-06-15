package com.potterlim.daymark.service;

public final class OperationalAlertMessage {

    private final String mValue;

    private OperationalAlertMessage(String value) {
        mValue = value;
    }

    public static OperationalAlertMessage empty() {
        return new OperationalAlertMessage("");
    }

    public static OperationalAlertMessage create(String valueOrNull) {
        if (valueOrNull == null) {
            return empty();
        }

        return new OperationalAlertMessage(valueOrNull);
    }

    public String getValue() {
        return mValue;
    }
}
