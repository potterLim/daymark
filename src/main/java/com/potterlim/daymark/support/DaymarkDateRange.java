package com.potterlim.daymark.support;

import java.time.LocalDate;

public final class DaymarkDateRange {

    private final LocalDate mStartDate;
    private final LocalDate mEndDate;

    private DaymarkDateRange(LocalDate startDate, LocalDate endDate) {
        mStartDate = startDate;
        mEndDate = endDate;
    }

    public static DaymarkDateRange of(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("startDate must not be null.");
        }

        if (endDate == null) {
            throw new IllegalArgumentException("endDate must not be null.");
        }

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must not be before startDate.");
        }

        return new DaymarkDateRange(startDate, endDate);
    }

    public LocalDate getStartDate() {
        return mStartDate;
    }

    public LocalDate getEndDate() {
        return mEndDate;
    }
}
