package com.potterlim.daymark.dto.operations;

import java.time.LocalDate;
import com.potterlim.daymark.entity.WeeklyOperationMetricSnapshot;
import com.potterlim.daymark.support.WeeklyOperationMetricCount;
import com.potterlim.daymark.support.WeeklyOperationMetricPercent;
import com.potterlim.daymark.support.WeeklyOperationsSummary;

final class OperationsTrendRawPointDto {

    private final LocalDate mWeekStartDate;
    private final LocalDate mWeekEndDate;
    private final WeeklyOperationMetricCount mTotalRegisteredUsers;
    private final WeeklyOperationMetricCount mNewlyRegisteredUsers;
    private final WeeklyOperationMetricCount mWeeklyActiveUsers;
    private final WeeklyOperationMetricCount mWeeklyWritingUsers;
    private final WeeklyOperationMetricCount mWeeklyWritingDays;
    private final WeeklyOperationMetricCount mMarkdownExportedCount;
    private final WeeklyOperationMetricCount mPdfExportViewedCount;
    private final WeeklyOperationMetricPercent mGoalCompletionRatePercent;

    private OperationsTrendRawPointDto(
        LocalDate weekStartDate,
        LocalDate weekEndDate,
        WeeklyOperationMetricCount totalRegisteredUsers,
        WeeklyOperationMetricCount newlyRegisteredUsers,
        WeeklyOperationMetricCount weeklyActiveUsers,
        WeeklyOperationMetricCount weeklyWritingUsers,
        WeeklyOperationMetricCount weeklyWritingDays,
        WeeklyOperationMetricCount markdownExportedCount,
        WeeklyOperationMetricCount pdfExportViewedCount,
        WeeklyOperationMetricPercent goalCompletionRatePercent
    ) {
        if (weekStartDate == null) {
            throw new IllegalArgumentException("weekStartDate must not be null.");
        }

        if (weekEndDate == null) {
            throw new IllegalArgumentException("weekEndDate must not be null.");
        }

        mWeekStartDate = weekStartDate;
        mWeekEndDate = weekEndDate;
        mTotalRegisteredUsers = requireMetricCount(totalRegisteredUsers, "totalRegisteredUsers");
        mNewlyRegisteredUsers = requireMetricCount(newlyRegisteredUsers, "newlyRegisteredUsers");
        mWeeklyActiveUsers = requireMetricCount(weeklyActiveUsers, "weeklyActiveUsers");
        mWeeklyWritingUsers = requireMetricCount(weeklyWritingUsers, "weeklyWritingUsers");
        mWeeklyWritingDays = requireMetricCount(weeklyWritingDays, "weeklyWritingDays");
        mMarkdownExportedCount = requireMetricCount(markdownExportedCount, "markdownExportedCount");
        mPdfExportViewedCount = requireMetricCount(pdfExportViewedCount, "pdfExportViewedCount");
        mGoalCompletionRatePercent = requireMetricPercent(goalCompletionRatePercent, "goalCompletionRatePercent");
    }

    static OperationsTrendRawPointDto createFromSnapshot(
        WeeklyOperationMetricSnapshot weeklyOperationMetricSnapshot
    ) {
        if (weeklyOperationMetricSnapshot == null) {
            throw new IllegalArgumentException("weeklyOperationMetricSnapshot must not be null.");
        }

        return new OperationsTrendRawPointDto(
            weeklyOperationMetricSnapshot.getWeekStartDate(),
            weeklyOperationMetricSnapshot.getWeekEndDate(),
            WeeklyOperationMetricCount.of(weeklyOperationMetricSnapshot.getTotalRegisteredUsers()),
            WeeklyOperationMetricCount.of(weeklyOperationMetricSnapshot.getNewlyRegisteredUsers()),
            WeeklyOperationMetricCount.of(weeklyOperationMetricSnapshot.getWeeklyActiveUsers()),
            WeeklyOperationMetricCount.of(weeklyOperationMetricSnapshot.getWeeklyWritingUsers()),
            WeeklyOperationMetricCount.of(weeklyOperationMetricSnapshot.getWeeklyWritingDays()),
            WeeklyOperationMetricCount.of(weeklyOperationMetricSnapshot.getMarkdownExportedCount()),
            WeeklyOperationMetricCount.of(weeklyOperationMetricSnapshot.getPdfExportViewedCount()),
            WeeklyOperationMetricPercent.of(weeklyOperationMetricSnapshot.getGoalCompletionRatePercent())
        );
    }

    static OperationsTrendRawPointDto createFromSummary(WeeklyOperationsSummary weeklyOperationsSummary) {
        if (weeklyOperationsSummary == null) {
            throw new IllegalArgumentException("weeklyOperationsSummary must not be null.");
        }

        return new OperationsTrendRawPointDto(
            weeklyOperationsSummary.getWeekStartDate(),
            weeklyOperationsSummary.getWeekEndDate(),
            WeeklyOperationMetricCount.of(weeklyOperationsSummary.getTotalRegisteredUsers()),
            WeeklyOperationMetricCount.of(weeklyOperationsSummary.getNewlyRegisteredUsers()),
            WeeklyOperationMetricCount.of(weeklyOperationsSummary.getWeeklyActiveUsers()),
            WeeklyOperationMetricCount.of(weeklyOperationsSummary.getWeeklyWritingUsers()),
            WeeklyOperationMetricCount.of(weeklyOperationsSummary.getWeeklyWritingDays()),
            WeeklyOperationMetricCount.of(weeklyOperationsSummary.getMarkdownExportedCount()),
            WeeklyOperationMetricCount.of(weeklyOperationsSummary.getPdfExportViewedCount()),
            WeeklyOperationMetricPercent.of(weeklyOperationsSummary.getGoalCompletionRatePercent())
        );
    }

    LocalDate getWeekStartDate() {
        return mWeekStartDate;
    }

    LocalDate getWeekEndDate() {
        return mWeekEndDate;
    }

    long getTotalRegisteredUsers() {
        return mTotalRegisteredUsers.getValue();
    }

    long getNewlyRegisteredUsers() {
        return mNewlyRegisteredUsers.getValue();
    }

    long getWeeklyActiveUsers() {
        return mWeeklyActiveUsers.getValue();
    }

    long getWeeklyWritingUsers() {
        return mWeeklyWritingUsers.getValue();
    }

    long getWeeklyWritingDays() {
        return mWeeklyWritingDays.getValue();
    }

    long getMarkdownExportedCount() {
        return mMarkdownExportedCount.getValue();
    }

    long getPdfExportViewedCount() {
        return mPdfExportViewedCount.getValue();
    }

    long getExportCount() {
        return Math.addExact(getMarkdownExportedCount(), getPdfExportViewedCount());
    }

    double getGoalCompletionRatePercent() {
        return mGoalCompletionRatePercent.getValue();
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
