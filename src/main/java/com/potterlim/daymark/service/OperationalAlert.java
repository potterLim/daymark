package com.potterlim.daymark.service;

public final class OperationalAlert {

    private final EOperationalAlertType mAlertType;
    private final String mMessage;

    public OperationalAlert(EOperationalAlertType alertType, OperationalAlertMessage message) {
        if (alertType == null) {
            throw new IllegalArgumentException("alertType must not be null.");
        }

        if (message == null) {
            throw new IllegalArgumentException("message must not be null.");
        }

        mAlertType = alertType;
        mMessage = message.getValue();
    }

    public EOperationalAlertType getAlertType() {
        return mAlertType;
    }

    public String getMessage() {
        return mMessage;
    }
}
