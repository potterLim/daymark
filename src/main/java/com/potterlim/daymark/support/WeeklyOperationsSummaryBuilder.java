package com.potterlim.daymark.support;

public final class WeeklyOperationsSummaryBuilder {

    private final DaymarkWeekRange mWeekRange;
    private WeeklyOperationMetricCount mTotalRegisteredUsers = WeeklyOperationMetricCount.zero();
    private WeeklyOperationMetricCount mNewlyRegisteredUsers = WeeklyOperationMetricCount.zero();
    private WeeklyOperationMetricCount mWeeklyActiveUsers = WeeklyOperationMetricCount.zero();
    private WeeklyOperationMetricCount mWeeklyWritingUsers = WeeklyOperationMetricCount.zero();
    private WeeklyOperationMetricCount mWeeklyWritingDays = WeeklyOperationMetricCount.zero();
    private WeeklyOperationMetricCount mWeeklyMorningEntries = WeeklyOperationMetricCount.zero();
    private WeeklyOperationMetricCount mWeeklyEveningEntries = WeeklyOperationMetricCount.zero();
    private WeeklyOperationMetricCount mWeeklyPlanReviewCompletedDays = WeeklyOperationMetricCount.zero();
    private WeeklyOperationMetricCount mSignInSucceededCount = WeeklyOperationMetricCount.zero();
    private WeeklyOperationMetricCount mSignInFailedCount = WeeklyOperationMetricCount.zero();
    private WeeklyOperationMetricCount mWeeklyReviewViewedCount = WeeklyOperationMetricCount.zero();
    private WeeklyOperationMetricCount mRecordLibraryViewedCount = WeeklyOperationMetricCount.zero();
    private WeeklyOperationMetricCount mMarkdownExportedCount = WeeklyOperationMetricCount.zero();
    private WeeklyOperationMetricCount mPdfExportViewedCount = WeeklyOperationMetricCount.zero();
    private WeeklyOperationMetricCount mExportingUsers = WeeklyOperationMetricCount.zero();
    private WeeklyOperationMetricCount mNewWorkspaceActivatedUsers = WeeklyOperationMetricCount.zero();
    private WeeklyOperationMetricAverage mAverageWritingDaysPerActiveUser = WeeklyOperationMetricAverage.zero();
    private WeeklyOperationMetricAverage mAverageEntryCompletionsPerActiveUser = WeeklyOperationMetricAverage.zero();
    private WeeklyOperationMetricPercent mPlanReviewConversionRatePercent = WeeklyOperationMetricPercent.zero();
    private WeeklyOperationMetricPercent mNewWorkspaceActivationRatePercent = WeeklyOperationMetricPercent.zero();
    private WeeklyOperationMetricPercent mGoalCompletionRatePercent = WeeklyOperationMetricPercent.zero();

    WeeklyOperationsSummaryBuilder(DaymarkWeekRange weekRange) {
        if (weekRange == null) {
            throw new IllegalArgumentException("weekRange must not be null.");
        }

        mWeekRange = weekRange;
    }

    public WeeklyOperationsSummaryBuilder setTotalRegisteredUsers(WeeklyOperationMetricCount totalRegisteredUsers) {
        mTotalRegisteredUsers = requireMetricCount(totalRegisteredUsers, "totalRegisteredUsers");
        return this;
    }

    public WeeklyOperationsSummaryBuilder setNewlyRegisteredUsers(WeeklyOperationMetricCount newlyRegisteredUsers) {
        mNewlyRegisteredUsers = requireMetricCount(newlyRegisteredUsers, "newlyRegisteredUsers");
        return this;
    }

    public WeeklyOperationsSummaryBuilder setWeeklyActiveUsers(WeeklyOperationMetricCount weeklyActiveUsers) {
        mWeeklyActiveUsers = requireMetricCount(weeklyActiveUsers, "weeklyActiveUsers");
        return this;
    }

    public WeeklyOperationsSummaryBuilder setWeeklyWritingUsers(WeeklyOperationMetricCount weeklyWritingUsers) {
        mWeeklyWritingUsers = requireMetricCount(weeklyWritingUsers, "weeklyWritingUsers");
        return this;
    }

    public WeeklyOperationsSummaryBuilder setWeeklyWritingDays(WeeklyOperationMetricCount weeklyWritingDays) {
        mWeeklyWritingDays = requireMetricCount(weeklyWritingDays, "weeklyWritingDays");
        return this;
    }

    public WeeklyOperationsSummaryBuilder setWeeklyMorningEntries(WeeklyOperationMetricCount weeklyMorningEntries) {
        mWeeklyMorningEntries = requireMetricCount(weeklyMorningEntries, "weeklyMorningEntries");
        return this;
    }

    public WeeklyOperationsSummaryBuilder setWeeklyEveningEntries(WeeklyOperationMetricCount weeklyEveningEntries) {
        mWeeklyEveningEntries = requireMetricCount(weeklyEveningEntries, "weeklyEveningEntries");
        return this;
    }

    public WeeklyOperationsSummaryBuilder setWeeklyPlanReviewCompletedDays(
        WeeklyOperationMetricCount weeklyPlanReviewCompletedDays
    ) {
        mWeeklyPlanReviewCompletedDays = requireMetricCount(
            weeklyPlanReviewCompletedDays,
            "weeklyPlanReviewCompletedDays"
        );
        return this;
    }

    public WeeklyOperationsSummaryBuilder setSignInSucceededCount(WeeklyOperationMetricCount signInSucceededCount) {
        mSignInSucceededCount = requireMetricCount(signInSucceededCount, "signInSucceededCount");
        return this;
    }

    public WeeklyOperationsSummaryBuilder setSignInFailedCount(WeeklyOperationMetricCount signInFailedCount) {
        mSignInFailedCount = requireMetricCount(signInFailedCount, "signInFailedCount");
        return this;
    }

    public WeeklyOperationsSummaryBuilder setWeeklyReviewViewedCount(
        WeeklyOperationMetricCount weeklyReviewViewedCount
    ) {
        mWeeklyReviewViewedCount = requireMetricCount(weeklyReviewViewedCount, "weeklyReviewViewedCount");
        return this;
    }

    public WeeklyOperationsSummaryBuilder setRecordLibraryViewedCount(
        WeeklyOperationMetricCount recordLibraryViewedCount
    ) {
        mRecordLibraryViewedCount = requireMetricCount(recordLibraryViewedCount, "recordLibraryViewedCount");
        return this;
    }

    public WeeklyOperationsSummaryBuilder setMarkdownExportedCount(WeeklyOperationMetricCount markdownExportedCount) {
        mMarkdownExportedCount = requireMetricCount(markdownExportedCount, "markdownExportedCount");
        return this;
    }

    public WeeklyOperationsSummaryBuilder setPdfExportViewedCount(WeeklyOperationMetricCount pdfExportViewedCount) {
        mPdfExportViewedCount = requireMetricCount(pdfExportViewedCount, "pdfExportViewedCount");
        return this;
    }

    public WeeklyOperationsSummaryBuilder setExportingUsers(WeeklyOperationMetricCount exportingUsers) {
        mExportingUsers = requireMetricCount(exportingUsers, "exportingUsers");
        return this;
    }

    public WeeklyOperationsSummaryBuilder setNewWorkspaceActivatedUsers(
        WeeklyOperationMetricCount newWorkspaceActivatedUsers
    ) {
        mNewWorkspaceActivatedUsers = requireMetricCount(newWorkspaceActivatedUsers, "newWorkspaceActivatedUsers");
        return this;
    }

    public WeeklyOperationsSummaryBuilder setAverageWritingDaysPerActiveUser(
        WeeklyOperationMetricAverage averageWritingDaysPerActiveUser
    ) {
        mAverageWritingDaysPerActiveUser = requireMetricAverage(
            averageWritingDaysPerActiveUser,
            "averageWritingDaysPerActiveUser"
        );
        return this;
    }

    public WeeklyOperationsSummaryBuilder setAverageEntryCompletionsPerActiveUser(
        WeeklyOperationMetricAverage averageEntryCompletionsPerActiveUser
    ) {
        mAverageEntryCompletionsPerActiveUser = requireMetricAverage(
            averageEntryCompletionsPerActiveUser,
            "averageEntryCompletionsPerActiveUser"
        );
        return this;
    }

    public WeeklyOperationsSummaryBuilder setPlanReviewConversionRatePercent(
        WeeklyOperationMetricPercent planReviewConversionRatePercent
    ) {
        mPlanReviewConversionRatePercent = requireMetricPercent(
            planReviewConversionRatePercent,
            "planReviewConversionRatePercent"
        );
        return this;
    }

    public WeeklyOperationsSummaryBuilder setNewWorkspaceActivationRatePercent(
        WeeklyOperationMetricPercent newWorkspaceActivationRatePercent
    ) {
        mNewWorkspaceActivationRatePercent = requireMetricPercent(
            newWorkspaceActivationRatePercent,
            "newWorkspaceActivationRatePercent"
        );
        return this;
    }

    public WeeklyOperationsSummaryBuilder setGoalCompletionRatePercent(
        WeeklyOperationMetricPercent goalCompletionRatePercent
    ) {
        mGoalCompletionRatePercent = requireMetricPercent(goalCompletionRatePercent, "goalCompletionRatePercent");
        return this;
    }

    public WeeklyOperationsSummary build() {
        return new WeeklyOperationsSummary(this);
    }

    DaymarkWeekRange getWeekRange() {
        return mWeekRange;
    }

    WeeklyOperationMetricCount getTotalRegisteredUsers() {
        return mTotalRegisteredUsers;
    }

    WeeklyOperationMetricCount getNewlyRegisteredUsers() {
        return mNewlyRegisteredUsers;
    }

    WeeklyOperationMetricCount getWeeklyActiveUsers() {
        return mWeeklyActiveUsers;
    }

    WeeklyOperationMetricCount getWeeklyWritingUsers() {
        return mWeeklyWritingUsers;
    }

    WeeklyOperationMetricCount getWeeklyWritingDays() {
        return mWeeklyWritingDays;
    }

    WeeklyOperationMetricCount getWeeklyMorningEntries() {
        return mWeeklyMorningEntries;
    }

    WeeklyOperationMetricCount getWeeklyEveningEntries() {
        return mWeeklyEveningEntries;
    }

    WeeklyOperationMetricCount getWeeklyPlanReviewCompletedDays() {
        return mWeeklyPlanReviewCompletedDays;
    }

    WeeklyOperationMetricCount getSignInSucceededCount() {
        return mSignInSucceededCount;
    }

    WeeklyOperationMetricCount getSignInFailedCount() {
        return mSignInFailedCount;
    }

    WeeklyOperationMetricCount getWeeklyReviewViewedCount() {
        return mWeeklyReviewViewedCount;
    }

    WeeklyOperationMetricCount getRecordLibraryViewedCount() {
        return mRecordLibraryViewedCount;
    }

    WeeklyOperationMetricCount getMarkdownExportedCount() {
        return mMarkdownExportedCount;
    }

    WeeklyOperationMetricCount getPdfExportViewedCount() {
        return mPdfExportViewedCount;
    }

    WeeklyOperationMetricCount getExportingUsers() {
        return mExportingUsers;
    }

    WeeklyOperationMetricCount getNewWorkspaceActivatedUsers() {
        return mNewWorkspaceActivatedUsers;
    }

    WeeklyOperationMetricAverage getAverageWritingDaysPerActiveUser() {
        return mAverageWritingDaysPerActiveUser;
    }

    WeeklyOperationMetricAverage getAverageEntryCompletionsPerActiveUser() {
        return mAverageEntryCompletionsPerActiveUser;
    }

    WeeklyOperationMetricPercent getPlanReviewConversionRatePercent() {
        return mPlanReviewConversionRatePercent;
    }

    WeeklyOperationMetricPercent getNewWorkspaceActivationRatePercent() {
        return mNewWorkspaceActivationRatePercent;
    }

    WeeklyOperationMetricPercent getGoalCompletionRatePercent() {
        return mGoalCompletionRatePercent;
    }

    private static WeeklyOperationMetricCount requireMetricCount(
        WeeklyOperationMetricCount metricCount,
        String metricName
    ) {
        if (metricCount == null) {
            throw new IllegalArgumentException(metricName + " must not be null.");
        }

        return metricCount;
    }

    private static WeeklyOperationMetricAverage requireMetricAverage(
        WeeklyOperationMetricAverage metricAverage,
        String metricName
    ) {
        if (metricAverage == null) {
            throw new IllegalArgumentException(metricName + " must not be null.");
        }

        return metricAverage;
    }

    private static WeeklyOperationMetricPercent requireMetricPercent(
        WeeklyOperationMetricPercent metricPercent,
        String metricName
    ) {
        if (metricPercent == null) {
            throw new IllegalArgumentException(metricName + " must not be null.");
        }

        return metricPercent;
    }
}
