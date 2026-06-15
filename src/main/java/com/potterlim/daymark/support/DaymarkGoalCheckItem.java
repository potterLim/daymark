package com.potterlim.daymark.support;

public final class DaymarkGoalCheckItem {

    private final String mText;
    private final boolean mIsDone;

    private DaymarkGoalCheckItem(String textOrNull, boolean isDone) {
        String normalizedText = "";
        if (textOrNull != null) {
            normalizedText = textOrNull.trim();
        }

        mText = normalizedText;
        mIsDone = isDone;
    }

    public static DaymarkGoalCheckItem createCompleted(String textOrNull) {
        return new DaymarkGoalCheckItem(textOrNull, true);
    }

    public static DaymarkGoalCheckItem createPending(String textOrNull) {
        return new DaymarkGoalCheckItem(textOrNull, false);
    }

    public String getText() {
        return mText;
    }

    public boolean isDone() {
        return mIsDone;
    }

    public boolean hasText() {
        return !mText.isEmpty();
    }
}
