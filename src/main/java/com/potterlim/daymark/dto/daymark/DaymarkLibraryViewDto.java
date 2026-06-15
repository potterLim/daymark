package com.potterlim.daymark.dto.daymark;

import java.time.LocalDate;
import java.util.List;
import com.potterlim.daymark.support.DaymarkGoalCompletionCounts;

public final class DaymarkLibraryViewDto {

    private final DaymarkLibrarySearchCriteria mSearchCriteria;
    private final List<DaymarkLibraryItemDto> mItems;
    private final List<DaymarkLibraryItemDto> mTrendItems;
    private final List<DaymarkLibraryCalendarDayDto> mCalendarDays;
    private final LocalDate mCalendarMonthDate;
    private final int mMorningEntryCount;
    private final int mEveningEntryCount;
    private final DaymarkGoalCompletionCounts mGoalCompletionCounts;

    private DaymarkLibraryViewDto(Builder builder) {
        mSearchCriteria = builder.mSearchCriteria;
        mItems = List.copyOf(builder.mItems);
        mTrendItems = List.copyOf(builder.mTrendItems);
        mCalendarDays = List.copyOf(builder.mCalendarDays);
        mCalendarMonthDate = builder.mCalendarMonthDate;
        mMorningEntryCount = builder.mMorningEntryCount;
        mEveningEntryCount = builder.mEveningEntryCount;
        mGoalCompletionCounts = builder.mGoalCompletionCounts;
    }

    public static Builder createBuilder(DaymarkLibrarySearchCriteria searchCriteria) {
        return new Builder(searchCriteria);
    }

    public DaymarkLibrarySearchCriteria getSearchCriteria() {
        return mSearchCriteria;
    }

    public List<DaymarkLibraryItemDto> getItems() {
        return mItems;
    }

    public List<DaymarkLibraryItemDto> getTrendItems() {
        return mTrendItems;
    }

    public List<DaymarkLibraryCalendarDayDto> getCalendarDays() {
        return mCalendarDays;
    }

    public LocalDate getCalendarMonthDate() {
        return mCalendarMonthDate;
    }

    public int getEntryCount() {
        return mItems.size();
    }

    public int getMorningEntryCount() {
        return mMorningEntryCount;
    }

    public int getEveningEntryCount() {
        return mEveningEntryCount;
    }

    public int getAchievedGoalCount() {
        return mGoalCompletionCounts.getCompletedGoalCount();
    }

    public int getTotalGoalCount() {
        return mGoalCompletionCounts.getTotalGoalCount();
    }

    public int getAverageGoalCompletionPercent() {
        return mGoalCompletionCounts.calculateCompletionPercent();
    }

    public boolean hasEntries() {
        return !mItems.isEmpty();
    }

    public static final class Builder {

        private final DaymarkLibrarySearchCriteria mSearchCriteria;
        private List<DaymarkLibraryItemDto> mItems = List.of();
        private List<DaymarkLibraryItemDto> mTrendItems = List.of();
        private List<DaymarkLibraryCalendarDayDto> mCalendarDays = List.of();
        private LocalDate mCalendarMonthDate;
        private int mMorningEntryCount;
        private int mEveningEntryCount;
        private DaymarkGoalCompletionCounts mGoalCompletionCounts = DaymarkGoalCompletionCounts.empty();

        private Builder(DaymarkLibrarySearchCriteria searchCriteria) {
            if (searchCriteria == null) {
                throw new IllegalArgumentException("searchCriteria must not be null.");
            }

            mSearchCriteria = searchCriteria;
        }

        public Builder setItems(List<DaymarkLibraryItemDto> items) {
            if (items == null) {
                throw new IllegalArgumentException("items must not be null.");
            }

            mItems = List.copyOf(items);
            return this;
        }

        public Builder setTrendItems(List<DaymarkLibraryItemDto> trendItems) {
            if (trendItems == null) {
                throw new IllegalArgumentException("trendItems must not be null.");
            }

            mTrendItems = List.copyOf(trendItems);
            return this;
        }

        public Builder setCalendarDays(List<DaymarkLibraryCalendarDayDto> calendarDays) {
            if (calendarDays == null) {
                throw new IllegalArgumentException("calendarDays must not be null.");
            }

            mCalendarDays = List.copyOf(calendarDays);
            return this;
        }

        public Builder setCalendarMonthDate(LocalDate calendarMonthDate) {
            if (calendarMonthDate == null) {
                throw new IllegalArgumentException("calendarMonthDate must not be null.");
            }

            mCalendarMonthDate = calendarMonthDate;
            return this;
        }

        public Builder setMorningEntryCount(int morningEntryCount) {
            mMorningEntryCount = requireNonNegative(morningEntryCount, "morningEntryCount");
            return this;
        }

        public Builder setEveningEntryCount(int eveningEntryCount) {
            mEveningEntryCount = requireNonNegative(eveningEntryCount, "eveningEntryCount");
            return this;
        }

        public Builder setGoalCompletionCounts(DaymarkGoalCompletionCounts goalCompletionCounts) {
            if (goalCompletionCounts == null) {
                throw new IllegalArgumentException("goalCompletionCounts must not be null.");
            }

            mGoalCompletionCounts = goalCompletionCounts;
            return this;
        }

        public DaymarkLibraryViewDto build() {
            if (mCalendarMonthDate == null) {
                throw new IllegalStateException("calendarMonthDate must be set.");
            }

            return new DaymarkLibraryViewDto(this);
        }

        private static int requireNonNegative(int value, String fieldName) {
            if (value < 0) {
                throw new IllegalArgumentException(fieldName + " must not be negative.");
            }

            return value;
        }
    }
}
