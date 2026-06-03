package com.potterlim.daymark.dto.daymark;

import java.time.LocalDate;

public final class WeeklyProgressItemDto {

    private final LocalDate mDate;
    private final int mAchievedGoalCount;
    private final int mTotalGoalCount;
    private final int mCompletionPercent;

    public WeeklyProgressItemDto(
        LocalDate date,
        int achievedGoalCount,
        int totalGoalCount,
        int completionPercent
    ) {
        mDate = date;
        mAchievedGoalCount = achievedGoalCount;
        mTotalGoalCount = totalGoalCount;
        mCompletionPercent = completionPercent;
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
