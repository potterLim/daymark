package com.potterlim.daymark.dto.daymark;

import java.time.LocalDate;

public final class DaymarkDayStatusDto {

    private final LocalDate mDate;
    private final boolean mHasMorningEntry;
    private final boolean mHasEveningEntry;

    private DaymarkDayStatusDto(LocalDate date, boolean hasMorningEntry, boolean hasEveningEntry) {
        mDate = date;
        mHasMorningEntry = hasMorningEntry;
        mHasEveningEntry = hasEveningEntry;
    }

    public static DaymarkDayStatusDto createMorningAndEvening(LocalDate date) {
        return new DaymarkDayStatusDto(date, true, true);
    }

    public static DaymarkDayStatusDto createMorningOnly(LocalDate date) {
        return new DaymarkDayStatusDto(date, true, false);
    }

    public static DaymarkDayStatusDto createEveningOnly(LocalDate date) {
        return new DaymarkDayStatusDto(date, false, true);
    }

    public LocalDate getDate() {
        return mDate;
    }

    public boolean hasMorningEntry() {
        return mHasMorningEntry;
    }

    public boolean hasEveningEntry() {
        return mHasEveningEntry;
    }
}
