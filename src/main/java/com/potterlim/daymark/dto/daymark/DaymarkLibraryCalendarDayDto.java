package com.potterlim.daymark.dto.daymark;

import java.time.LocalDate;

public final class DaymarkLibraryCalendarDayDto {

    private final LocalDate mDate;
    private final boolean mIsCurrentMonth;
    private final boolean mHasEntry;
    private final int mCompletionPercent;
    private final boolean mIsToday;

    private DaymarkLibraryCalendarDayDto(Builder builder) {
        mDate = builder.mDate;
        mIsCurrentMonth = builder.mIsCurrentMonth;
        mHasEntry = builder.mHasEntry;
        mCompletionPercent = builder.mCompletionPercent;
        mIsToday = builder.mIsToday;
    }

    public static Builder createBuilder(LocalDate date) {
        return new Builder(date);
    }

    public LocalDate getDate() {
        return mDate;
    }

    public int getDayOfMonth() {
        return mDate.getDayOfMonth();
    }

    public boolean isCurrentMonth() {
        return mIsCurrentMonth;
    }

    public boolean hasEntry() {
        return mHasEntry;
    }

    public int getCompletionPercent() {
        return mCompletionPercent;
    }

    public int getIntensityLevel() {
        if (!mHasEntry) {
            return 0;
        }

        if (mCompletionPercent >= 80) {
            return 3;
        }

        if (mCompletionPercent >= 40) {
            return 2;
        }

        return 1;
    }

    public boolean isToday() {
        return mIsToday;
    }

    public static final class Builder {

        private final LocalDate mDate;
        private boolean mIsCurrentMonth;
        private boolean mHasEntry;
        private int mCompletionPercent;
        private boolean mIsToday;

        private Builder(LocalDate date) {
            if (date == null) {
                throw new IllegalArgumentException("date must not be null.");
            }

            mDate = date;
        }

        public Builder markCurrentMonth() {
            mIsCurrentMonth = true;
            return this;
        }

        public Builder markEntryPresent() {
            mHasEntry = true;
            return this;
        }

        public Builder setCompletionPercent(int completionPercent) {
            if (completionPercent < 0 || completionPercent > 100) {
                throw new IllegalArgumentException("completionPercent must be between 0 and 100.");
            }

            mCompletionPercent = completionPercent;
            return this;
        }

        public Builder markToday() {
            mIsToday = true;
            return this;
        }

        public DaymarkLibraryCalendarDayDto build() {
            return new DaymarkLibraryCalendarDayDto(this);
        }
    }
}
