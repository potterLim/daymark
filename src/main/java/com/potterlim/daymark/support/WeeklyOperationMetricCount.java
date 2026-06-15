package com.potterlim.daymark.support;

public final class WeeklyOperationMetricCount {

    private static final WeeklyOperationMetricCount ZERO = new WeeklyOperationMetricCount(0L);

    private final long mValue;

    private WeeklyOperationMetricCount(long value) {
        mValue = value;
    }

    public static WeeklyOperationMetricCount of(long value) {
        if (value < 0L) {
            throw new IllegalArgumentException("weeklyOperationMetricCount must not be negative.");
        }

        if (value == 0L) {
            return ZERO;
        }

        return new WeeklyOperationMetricCount(value);
    }

    public static WeeklyOperationMetricCount zero() {
        return ZERO;
    }

    public long getValue() {
        return mValue;
    }
}
