package com.potterlim.daymark.dto.daymark;

import java.time.LocalDate;
import com.potterlim.daymark.support.DaymarkGoalCompletionCounts;

public final class WeeklyProgressItemDto {

    private final LocalDate mDate;
    private final int mAchievedGoalCount;
    private final int mTotalGoalCount;
    private final int mCompletionPercent;

    private WeeklyProgressItemDto(LocalDate date, DaymarkGoalCompletionCounts goalCompletionCounts) {
        mDate = date;
        mAchievedGoalCount = goalCompletionCounts.getCompletedGoalCount();
        mTotalGoalCount = goalCompletionCounts.getTotalGoalCount();
        mCompletionPercent = goalCompletionCounts.calculateCompletionPercent();
    }

    public static WeeklyProgressItemDto fromGoalCompletionCounts(
        LocalDate date,
        DaymarkGoalCompletionCounts goalCompletionCounts
    ) {
        if (date == null) {
            throw new IllegalArgumentException("date must not be null.");
        }

        if (goalCompletionCounts == null) {
            throw new IllegalArgumentException("goalCompletionCounts must not be null.");
        }

        return new WeeklyProgressItemDto(date, goalCompletionCounts);
    }

    public LocalDate getDate() {
        return mDate;
    }

    public int getAchievedGoalCount() {
        return mAchievedGoalCount;
    }

    public int getTotalGoalCount() {
        return mTotalGoalCount;
    }

    public int getCompletionPercent() {
        return mCompletionPercent;
    }
}
