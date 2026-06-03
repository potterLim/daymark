package com.potterlim.daymark.dto.daymark;

import java.time.LocalDate;
import java.util.List;

public final class WeeklyProgressViewDto {

    private final List<WeeklyProgressItemDto> mWeeklyProgressItems;
    private final int mWeeklyAchievedGoalCount;
    private final int mWeeklyTotalGoalCount;
    private final int mWeeklyCompletionPercent;
    private final int mWeekOffset;
    private final int mPreviousWeekOffset;
    private final int mNextWeekOffset;
    private final String mRangeLabel;
    private final String mCurrentWeekRangeLabel;
    private final String mPreviousWeekRangeLabel;
    private final String mNextWeekRangeLabel;
    private final LocalDate mDefaultDate;

    public WeeklyProgressViewDto(
        List<WeeklyProgressItemDto> weeklyProgressItems,
        int weeklyAchievedGoalCount,
        int weeklyTotalGoalCount,
        int weeklyCompletionPercent,
        int weekOffset,
        String rangeLabel,
        String currentWeekRangeLabel,
        String previousWeekRangeLabel,
        String nextWeekRangeLabel,
        LocalDate defaultDate
    ) {
        mWeeklyProgressItems = List.copyOf(weeklyProgressItems);
        mWeeklyAchievedGoalCount = weeklyAchievedGoalCount;
        mWeeklyTotalGoalCount = weeklyTotalGoalCount;
        mWeeklyCompletionPercent = weeklyCompletionPercent;
        mWeekOffset = weekOffset;
        mPreviousWeekOffset = weekOffset - 1;
        mNextWeekOffset = weekOffset + 1;
        mRangeLabel = rangeLabel;
        mCurrentWeekRangeLabel = currentWeekRangeLabel;
        mPreviousWeekRangeLabel = previousWeekRangeLabel;
        mNextWeekRangeLabel = nextWeekRangeLabel;
        mDefaultDate = defaultDate;
    }

    public List<WeeklyProgressItemDto> getWeeklyProgressItems() {
        return mWeeklyProgressItems;
    }

    public int getWeeklyAchievedGoalCount() {
        return mWeeklyAchievedGoalCount;
    }

    public int getWeeklyTotalGoalCount() {
        return mWeeklyTotalGoalCount;
    }

    public int getWeeklyCompletionPercent() {
        return mWeeklyCompletionPercent;
    }

    public int getWeekOffset() {
        return mWeekOffset;
    }

    public int getPreviousWeekOffset() {
        return mPreviousWeekOffset;
    }

    public int getNextWeekOffset() {
        return mNextWeekOffset;
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
}
