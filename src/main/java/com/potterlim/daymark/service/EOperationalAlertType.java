package com.potterlim.daymark.service;

public enum EOperationalAlertType {

    WEEKLY_OPERATIONS_SUMMARY_FAILED("weekly-operations-summary-failed");

    private final String mCode;

    EOperationalAlertType(String code) {
        mCode = code;
    }

    public String getCode() {
        return mCode;
    }
}
