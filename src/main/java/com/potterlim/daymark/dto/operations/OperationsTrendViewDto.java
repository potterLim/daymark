package com.potterlim.daymark.dto.operations;

import java.util.List;

public final class OperationsTrendViewDto {

    private final List<OperationsTrendPointDto> mTrendPoints;
    private final String mActiveUserLinePoints;
    private final String mWritingUserLinePoints;
    private final String mNewUserLinePoints;
    private final String mGoalCompletionLinePoints;
    private final String mActiveUserDeltaText;
    private final String mWritingUserDeltaText;
    private final String mExportDeltaText;
    private final String mGoalCompletionDeltaText;
    private final boolean mHasPreviousTrendPoint;

    OperationsTrendViewDto(
        List<OperationsTrendPointDto> trendPoints,
        String activeUserLinePoints,
        String writingUserLinePoints,
        String newUserLinePoints,
        String goalCompletionLinePoints,
        String activeUserDeltaText,
        String writingUserDeltaText,
        String exportDeltaText,
        String goalCompletionDeltaText,
        boolean hasPreviousTrendPoint
    ) {
        if (trendPoints == null) {
            throw new IllegalArgumentException("trendPoints must not be null.");
        }

        mTrendPoints = List.copyOf(trendPoints);
        mActiveUserLinePoints = defaultToEmpty(activeUserLinePoints);
        mWritingUserLinePoints = defaultToEmpty(writingUserLinePoints);
        mNewUserLinePoints = defaultToEmpty(newUserLinePoints);
        mGoalCompletionLinePoints = defaultToEmpty(goalCompletionLinePoints);
        mActiveUserDeltaText = defaultToEmpty(activeUserDeltaText);
        mWritingUserDeltaText = defaultToEmpty(writingUserDeltaText);
        mExportDeltaText = defaultToEmpty(exportDeltaText);
        mGoalCompletionDeltaText = defaultToEmpty(goalCompletionDeltaText);
        mHasPreviousTrendPoint = hasPreviousTrendPoint;
    }

    public List<OperationsTrendPointDto> getTrendPoints() {
        return mTrendPoints;
    }

    public String getActiveUserLinePoints() {
        return mActiveUserLinePoints;
    }

    public String getWritingUserLinePoints() {
        return mWritingUserLinePoints;
    }

    public String getNewUserLinePoints() {
        return mNewUserLinePoints;
    }

    public String getGoalCompletionLinePoints() {
        return mGoalCompletionLinePoints;
    }

    public String getActiveUserDeltaText() {
        return mActiveUserDeltaText;
    }

    public String getWritingUserDeltaText() {
        return mWritingUserDeltaText;
    }

    public String getExportDeltaText() {
        return mExportDeltaText;
    }

    public String getGoalCompletionDeltaText() {
        return mGoalCompletionDeltaText;
    }

    public boolean hasPreviousTrendPoint() {
        return mHasPreviousTrendPoint;
    }

    private static String defaultToEmpty(String valueOrNull) {
        if (valueOrNull == null) {
            return "";
        }

        return valueOrNull;
    }
}
