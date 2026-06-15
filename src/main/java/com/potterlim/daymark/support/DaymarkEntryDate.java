package com.potterlim.daymark.support;

import java.time.LocalDate;

public final class DaymarkEntryDate {

    private final LocalDate mValue;

    private DaymarkEntryDate(LocalDate value) {
        mValue = value;
    }

    public static DaymarkEntryDate of(LocalDate value) {
        if (value == null) {
            throw new IllegalArgumentException("value must not be null.");
        }

        return new DaymarkEntryDate(value);
    }

    public DaymarkWeekRange containingWeekRange() {
        return DaymarkWeekRange.containing(mValue);
    }

    public LocalDate getValue() {
        return mValue;
    }
}
