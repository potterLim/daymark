package com.potterlim.daymark.support;

import java.time.DayOfWeek;
import java.time.LocalDate;

public final class DaymarkWeekRange {

    private final LocalDate mStartDate;
    private final LocalDate mEndDate;

    private DaymarkWeekRange(LocalDate startDate, LocalDate endDate) {
        mStartDate = startDate;
        mEndDate = endDate;
    }

    public static DaymarkWeekRange of(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("startDate must not be null.");
        }

        if (endDate == null) {
            throw new IllegalArgumentException("endDate must not be null.");
        }

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must not be before startDate.");
        }

        return new DaymarkWeekRange(startDate, endDate);
    }

    public static DaymarkWeekRange containing(LocalDate referenceDate) {
        if (referenceDate == null) {
            throw new IllegalArgumentException("referenceDate must not be null.");
        }

        LocalDate weekStartDate = startDateOfWeekContaining(referenceDate);
        return new DaymarkWeekRange(weekStartDate, weekStartDate.plusDays(6L));
    }

    public static LocalDate startDateOfWeekContaining(LocalDate referenceDate) {
        if (referenceDate == null) {
            throw new IllegalArgumentException("referenceDate must not be null.");
        }

        return referenceDate.minusDays(referenceDate.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue());
    }

    public DaymarkWeekRange withEndNoLaterThan(LocalDate latestEndDate) {
        if (latestEndDate == null || !mEndDate.isAfter(latestEndDate)) {
            return this;
        }

        return DaymarkWeekRange.of(mStartDate, latestEndDate);
    }

    public LocalDate getStartDate() {
        return mStartDate;
    }

    public LocalDate getEndDate() {
        return mEndDate;
    }
}
