package com.potterlim.daymark.support;

import java.time.LocalDate;

public final class DaymarkWeekOffset {

    private final int mValue;

    private DaymarkWeekOffset(int value) {
        mValue = value;
    }

    public static DaymarkWeekOffset of(int value) {
        return new DaymarkWeekOffset(value);
    }

    public DaymarkWeekOffset previous() {
        return new DaymarkWeekOffset(Math.subtractExact(mValue, 1));
    }

    public DaymarkWeekOffset next() {
        return new DaymarkWeekOffset(Math.addExact(mValue, 1));
    }

    public LocalDate calculateReferenceDateFrom(LocalDate currentDate) {
        if (currentDate == null) {
            throw new IllegalArgumentException("currentDate must not be null.");
        }

        return currentDate.plusDays((long) mValue * 7L);
    }

    public int getValue() {
        return mValue;
    }
}
