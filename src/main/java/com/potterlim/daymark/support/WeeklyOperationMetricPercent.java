package com.potterlim.daymark.support;

public final class WeeklyOperationMetricPercent {

    private static final WeeklyOperationMetricPercent ZERO = new WeeklyOperationMetricPercent(0.0);

    private final double mValue;

    private WeeklyOperationMetricPercent(double value) {
        mValue = value;
    }

    public static WeeklyOperationMetricPercent of(double value) {
        if (!Double.isFinite(value) || value < 0.0 || value > 100.0) {
            throw new IllegalArgumentException("weeklyOperationMetricPercent must be between 0 and 100.");
        }

        if (value == 0.0) {
            return ZERO;
        }

        return new WeeklyOperationMetricPercent(value);
    }

    public static WeeklyOperationMetricPercent zero() {
        return ZERO;
    }

    public static WeeklyOperationMetricPercent calculate(
        WeeklyOperationMetricCount numeratorCount,
        WeeklyOperationMetricCount denominatorCount
    ) {
        if (numeratorCount == null) {
            throw new IllegalArgumentException("numeratorCount must not be null.");
        }

        if (denominatorCount == null) {
            throw new IllegalArgumentException("denominatorCount must not be null.");
        }

        long denominatorValue = denominatorCount.getValue();
        if (denominatorValue == 0L) {
            return ZERO;
        }

        return of((double) numeratorCount.getValue() * 100.0 / denominatorValue);
    }

    public double getValue() {
        return mValue;
    }
}
