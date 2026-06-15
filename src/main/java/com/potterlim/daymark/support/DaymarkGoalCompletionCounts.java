package com.potterlim.daymark.support;

public final class DaymarkGoalCompletionCounts {

    private final int mCompletedGoalCount;
    private final int mTotalGoalCount;

    private DaymarkGoalCompletionCounts(int completedGoalCount, int totalGoalCount) {
        mCompletedGoalCount = completedGoalCount;
        mTotalGoalCount = totalGoalCount;
    }

    public static DaymarkGoalCompletionCounts empty() {
        return new DaymarkGoalCompletionCounts(0, 0);
    }

    public static DaymarkGoalCompletionCounts of(int completedGoalCount, int totalGoalCount) {
        if (completedGoalCount < 0) {
            throw new IllegalArgumentException("completedGoalCount must not be negative.");
        }

        if (totalGoalCount < 0) {
            throw new IllegalArgumentException("totalGoalCount must not be negative.");
        }

        if (completedGoalCount > totalGoalCount) {
            throw new IllegalArgumentException("completedGoalCount must not exceed totalGoalCount.");
        }

        return new DaymarkGoalCompletionCounts(completedGoalCount, totalGoalCount);
    }

    public DaymarkGoalCompletionCounts plus(DaymarkGoalCompletionCounts otherCounts) {
        if (otherCounts == null) {
            throw new IllegalArgumentException("otherCounts must not be null.");
        }

        return DaymarkGoalCompletionCounts.of(
            mCompletedGoalCount + otherCounts.getCompletedGoalCount(),
            mTotalGoalCount + otherCounts.getTotalGoalCount()
        );
    }

    public int calculateCompletionPercent() {
        if (mTotalGoalCount == 0) {
            return 0;
        }

        return (int) ((mCompletedGoalCount / (double) mTotalGoalCount) * 100);
    }

    public double calculateCompletionRatePercent() {
        if (mTotalGoalCount == 0) {
            return 0.0;
        }

        return (double) mCompletedGoalCount * 100.0 / mTotalGoalCount;
    }

    public int getCompletedGoalCount() {
        return mCompletedGoalCount;
    }

    public int getTotalGoalCount() {
        return mTotalGoalCount;
    }
}
