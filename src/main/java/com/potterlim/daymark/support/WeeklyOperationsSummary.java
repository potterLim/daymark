package com.potterlim.daymark.support;

import java.time.LocalDate;

public final class WeeklyOperationsSummary {

    private final DaymarkWeekRange mWeekRange;
    private final WeeklyOperationMetricCount mTotalRegisteredUsers;
    private final WeeklyOperationMetricCount mNewlyRegisteredUsers;
    private final WeeklyOperationMetricCount mWeeklyActiveUsers;
    private final WeeklyOperationMetricCount mWeeklyWritingUsers;
    private final WeeklyOperationMetricCount mWeeklyWritingDays;
    private final WeeklyOperationMetricCount mWeeklyMorningEntries;
    private final WeeklyOperationMetricCount mWeeklyEveningEntries;
    private final WeeklyOperationMetricCount mWeeklyPlanReviewCompletedDays;
    private final WeeklyOperationMetricCount mSignInSucceededCount;
    private final WeeklyOperationMetricCount mSignInFailedCount;
    private final WeeklyOperationMetricCount mWeeklyReviewViewedCount;
    private final WeeklyOperationMetricCount mRecordLibraryViewedCount;
    private final WeeklyOperationMetricCount mMarkdownExportedCount;
    private final WeeklyOperationMetricCount mPdfExportViewedCount;
    private final WeeklyOperationMetricCount mExportingUsers;
    private final WeeklyOperationMetricCount mNewWorkspaceActivatedUsers;
    private final WeeklyOperationMetricAverage mAverageWritingDaysPerActiveUser;
    private final WeeklyOperationMetricAverage mAverageEntryCompletionsPerActiveUser;
    private final WeeklyOperationMetricPercent mPlanReviewConversionRatePercent;
    private final WeeklyOperationMetricPercent mNewWorkspaceActivationRatePercent;
    private final WeeklyOperationMetricPercent mGoalCompletionRatePercent;

    WeeklyOperationsSummary(WeeklyOperationsSummaryBuilder builder) {
        mWeekRange = builder.getWeekRange();
        mTotalRegisteredUsers = builder.getTotalRegisteredUsers();
        mNewlyRegisteredUsers = builder.getNewlyRegisteredUsers();
        mWeeklyActiveUsers = builder.getWeeklyActiveUsers();
        mWeeklyWritingUsers = builder.getWeeklyWritingUsers();
        mWeeklyWritingDays = builder.getWeeklyWritingDays();
        mWeeklyMorningEntries = builder.getWeeklyMorningEntries();
        mWeeklyEveningEntries = builder.getWeeklyEveningEntries();
        mWeeklyPlanReviewCompletedDays = builder.getWeeklyPlanReviewCompletedDays();
        mSignInSucceededCount = builder.getSignInSucceededCount();
        mSignInFailedCount = builder.getSignInFailedCount();
        mWeeklyReviewViewedCount = builder.getWeeklyReviewViewedCount();
        mRecordLibraryViewedCount = builder.getRecordLibraryViewedCount();
        mMarkdownExportedCount = builder.getMarkdownExportedCount();
        mPdfExportViewedCount = builder.getPdfExportViewedCount();
        mExportingUsers = builder.getExportingUsers();
        mNewWorkspaceActivatedUsers = builder.getNewWorkspaceActivatedUsers();
        mAverageWritingDaysPerActiveUser = builder.getAverageWritingDaysPerActiveUser();
        mAverageEntryCompletionsPerActiveUser = builder.getAverageEntryCompletionsPerActiveUser();
        mPlanReviewConversionRatePercent = builder.getPlanReviewConversionRatePercent();
        mNewWorkspaceActivationRatePercent = builder.getNewWorkspaceActivationRatePercent();
        mGoalCompletionRatePercent = builder.getGoalCompletionRatePercent();
    }

    public static WeeklyOperationsSummaryBuilder createBuilder(DaymarkWeekRange weekRange) {
        return new WeeklyOperationsSummaryBuilder(weekRange);
    }

    public LocalDate getWeekStartDate() {
        return mWeekRange.getStartDate();
    }

    public LocalDate getWeekEndDate() {
        return mWeekRange.getEndDate();
    }

    public long getTotalRegisteredUsers() {
        return mTotalRegisteredUsers.getValue();
    }

    public long getNewlyRegisteredUsers() {
        return mNewlyRegisteredUsers.getValue();
    }

    public long getWeeklyActiveUsers() {
        return mWeeklyActiveUsers.getValue();
    }

    public long getWeeklyWritingUsers() {
        return mWeeklyWritingUsers.getValue();
    }

    public long getWeeklyWritingDays() {
        return mWeeklyWritingDays.getValue();
    }

    public long getWeeklyMorningEntries() {
        return mWeeklyMorningEntries.getValue();
    }

    public long getWeeklyEveningEntries() {
        return mWeeklyEveningEntries.getValue();
    }

    public long getWeeklyPlanReviewCompletedDays() {
        return mWeeklyPlanReviewCompletedDays.getValue();
    }

    public long getSignInSucceededCount() {
        return mSignInSucceededCount.getValue();
    }

    public long getSignInFailedCount() {
        return mSignInFailedCount.getValue();
    }

    public long getWeeklyReviewViewedCount() {
        return mWeeklyReviewViewedCount.getValue();
    }

    public long getRecordLibraryViewedCount() {
        return mRecordLibraryViewedCount.getValue();
    }

    public long getMarkdownExportedCount() {
        return mMarkdownExportedCount.getValue();
    }

    public long getPdfExportViewedCount() {
        return mPdfExportViewedCount.getValue();
    }

    public long getExportCount() {
        return getMarkdownExportedCount() + getPdfExportViewedCount();
    }

    public long getExportingUsers() {
        return mExportingUsers.getValue();
    }

    public long getNewWorkspaceActivatedUsers() {
        return mNewWorkspaceActivatedUsers.getValue();
    }

    public double getAverageWritingDaysPerActiveUser() {
        return mAverageWritingDaysPerActiveUser.getValue();
    }

    public double getAverageEntryCompletionsPerActiveUser() {
        return mAverageEntryCompletionsPerActiveUser.getValue();
    }

    public double getPlanReviewConversionRatePercent() {
        return mPlanReviewConversionRatePercent.getValue();
    }

    public double getNewWorkspaceActivationRatePercent() {
        return mNewWorkspaceActivationRatePercent.getValue();
    }

    public double getGoalCompletionRatePercent() {
        return mGoalCompletionRatePercent.getValue();
    }
}
