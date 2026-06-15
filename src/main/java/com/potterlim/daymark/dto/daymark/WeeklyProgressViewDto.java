package com.potterlim.daymark.dto.daymark;

import java.time.LocalDate;
import java.util.List;
import com.potterlim.daymark.support.DaymarkGoalCompletionCounts;
import com.potterlim.daymark.support.DaymarkWeekOffset;

public final class WeeklyProgressViewDto {

    private final List<WeeklyProgressItemDto> mWeeklyProgressItems;
    private final DaymarkGoalCompletionCounts mWeeklyGoalCompletionCounts;
    private final DaymarkWeekOffset mWeekOffset;
    private final String mRangeLabel;
    private final String mCurrentWeekRangeLabel;
    private final String mPreviousWeekRangeLabel;
    private final String mNextWeekRangeLabel;
    private final LocalDate mDefaultDate;

    private WeeklyProgressViewDto(Builder builder) {
        mWeeklyProgressItems = List.copyOf(builder.mWeeklyProgressItems);
        mWeeklyGoalCompletionCounts = builder.mWeeklyGoalCompletionCounts;
        mWeekOffset = builder.mWeekOffset;
        mRangeLabel = builder.mRangeLabel;
        mCurrentWeekRangeLabel = builder.mCurrentWeekRangeLabel;
        mPreviousWeekRangeLabel = builder.mPreviousWeekRangeLabel;
        mNextWeekRangeLabel = builder.mNextWeekRangeLabel;
        mDefaultDate = builder.mDefaultDate;
    }

    public static Builder createBuilder(DaymarkWeekOffset weekOffset) {
        return new Builder(weekOffset);
    }

    public List<WeeklyProgressItemDto> getWeeklyProgressItems() {
        return mWeeklyProgressItems;
    }

    public int getWeeklyAchievedGoalCount() {
        return mWeeklyGoalCompletionCounts.getCompletedGoalCount();
    }

    public int getWeeklyTotalGoalCount() {
        return mWeeklyGoalCompletionCounts.getTotalGoalCount();
    }

    public int getWeeklyCompletionPercent() {
        return mWeeklyGoalCompletionCounts.calculateCompletionPercent();
    }

    public int getWeekOffset() {
        return mWeekOffset.getValue();
    }

    public int getPreviousWeekOffset() {
        return mWeekOffset.previous().getValue();
    }

    public int getNextWeekOffset() {
        return mWeekOffset.next().getValue();
    }

    public String getRangeLabel() {
        return mRangeLabel;
    }

    public String getCurrentWeekRangeLabel() {
        return mCurrentWeekRangeLabel;
    }

    public String getPreviousWeekRangeLabel() {
        return mPreviousWeekRangeLabel;
    }

    public String getNextWeekRangeLabel() {
        return mNextWeekRangeLabel;
    }

    public LocalDate getDefaultDate() {
        return mDefaultDate;
    }

    public static final class Builder {

        private final DaymarkWeekOffset mWeekOffset;
        private List<WeeklyProgressItemDto> mWeeklyProgressItems = List.of();
        private DaymarkGoalCompletionCounts mWeeklyGoalCompletionCounts = DaymarkGoalCompletionCounts.empty();
        private String mRangeLabel = "";
        private String mCurrentWeekRangeLabel = "";
        private String mPreviousWeekRangeLabel = "";
        private String mNextWeekRangeLabel = "";
        private LocalDate mDefaultDate;

        private Builder(DaymarkWeekOffset weekOffset) {
            if (weekOffset == null) {
                throw new IllegalArgumentException("weekOffset must not be null.");
            }

            mWeekOffset = weekOffset;
        }

        public Builder setWeeklyProgressItems(List<WeeklyProgressItemDto> weeklyProgressItems) {
            if (weeklyProgressItems == null) {
                throw new IllegalArgumentException("weeklyProgressItems must not be null.");
            }

            mWeeklyProgressItems = List.copyOf(weeklyProgressItems);
            return this;
        }

        public Builder setWeeklyGoalCompletionCounts(DaymarkGoalCompletionCounts weeklyGoalCompletionCounts) {
            if (weeklyGoalCompletionCounts == null) {
                throw new IllegalArgumentException("weeklyGoalCompletionCounts must not be null.");
            }

            mWeeklyGoalCompletionCounts = weeklyGoalCompletionCounts;
            return this;
        }

        public Builder setRangeLabel(String rangeLabelOrNull) {
            mRangeLabel = rangeLabelOrNull == null ? "" : rangeLabelOrNull;
            return this;
        }

        public Builder setCurrentWeekRangeLabel(String currentWeekRangeLabelOrNull) {
            mCurrentWeekRangeLabel = currentWeekRangeLabelOrNull == null ? "" : currentWeekRangeLabelOrNull;
            return this;
        }

        public Builder setPreviousWeekRangeLabel(String previousWeekRangeLabelOrNull) {
            mPreviousWeekRangeLabel = previousWeekRangeLabelOrNull == null ? "" : previousWeekRangeLabelOrNull;
            return this;
        }

        public Builder setNextWeekRangeLabel(String nextWeekRangeLabelOrNull) {
            mNextWeekRangeLabel = nextWeekRangeLabelOrNull == null ? "" : nextWeekRangeLabelOrNull;
            return this;
        }

        public Builder setDefaultDate(LocalDate defaultDate) {
            if (defaultDate == null) {
                throw new IllegalArgumentException("defaultDate must not be null.");
            }

            mDefaultDate = defaultDate;
            return this;
        }

        public WeeklyProgressViewDto build() {
            if (mDefaultDate == null) {
                throw new IllegalStateException("defaultDate must be set.");
            }

            return new WeeklyProgressViewDto(this);
        }
    }
}
