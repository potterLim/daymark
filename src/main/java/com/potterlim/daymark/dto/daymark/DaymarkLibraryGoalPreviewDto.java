package com.potterlim.daymark.dto.daymark;

public final class DaymarkLibraryGoalPreviewDto {

    private final String mText;
    private final boolean mIsDone;

    private DaymarkLibraryGoalPreviewDto(String text, boolean isDone) {
        mText = text;
        mIsDone = isDone;
    }

    public static DaymarkLibraryGoalPreviewDto createCompleted(String text) {
        return new DaymarkLibraryGoalPreviewDto(text, true);
    }

    public static DaymarkLibraryGoalPreviewDto createPending(String text) {
        return new DaymarkLibraryGoalPreviewDto(text, false);
    }

    public String getText() {
        return mText;
    }

    public boolean isDone() {
        return mIsDone;
    }
}
