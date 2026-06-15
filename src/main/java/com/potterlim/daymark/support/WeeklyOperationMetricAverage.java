package com.potterlim.daymark.support;

public final class WeeklyOperationMetricAverage {

    private static final WeeklyOperationMetricAverage ZERO = new WeeklyOperationMetricAverage(0.0);

    private final double mValue;

    private WeeklyOperationMetricAverage(double value) {
        mValue = value;
    }

    public static WeeklyOperationMetricAverage of(double value) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException("weeklyOperationMetricAverage must be a non-negative finite value.");
        }

        if (value == 0.0) {
            return ZERO;
        }

        return new WeeklyOperationMetricAverage(value);
    }

    public static WeeklyOperationMetricAverage zero() {
        return ZERO;
    }

    public static WeeklyOperationMetricAverage calculate(
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

        return of((double) numeratorCount.getValue() / denominatorValue);
    }

    public double getValue() {
        return mValue;
    }
}
