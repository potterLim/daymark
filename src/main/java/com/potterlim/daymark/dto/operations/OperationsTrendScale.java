package com.potterlim.daymark.dto.operations;

import java.util.List;
import com.potterlim.daymark.support.WeeklyOperationMetricCount;

final class OperationsTrendScale {

    private final WeeklyOperationMetricCount mMaximumUserCount;
    private final WeeklyOperationMetricCount mMaximumExportCount;

    private OperationsTrendScale(
        WeeklyOperationMetricCount maximumUserCount,
        WeeklyOperationMetricCount maximumExportCount
    ) {
        mMaximumUserCount = requireMetricCount(maximumUserCount, "maximumUserCount");
        mMaximumExportCount = requireMetricCount(maximumExportCount, "maximumExportCount");
    }

    static OperationsTrendScale create(List<OperationsTrendRawPointDto> rawTrendPoints) {
        if (rawTrendPoints == null) {
            throw new IllegalArgumentException("rawTrendPoints must not be null.");
        }

        long maximumUserCount = 1L;
        long maximumExportCount = 1L;
        for (OperationsTrendRawPointDto rawTrendPoint : rawTrendPoints) {
            if (rawTrendPoint == null) {
                throw new IllegalArgumentException("rawTrendPoints must not contain null.");
            }

            maximumUserCount = Math.max(maximumUserCount, rawTrendPoint.getWeeklyActiveUsers());
            maximumUserCount = Math.max(maximumUserCount, rawTrendPoint.getWeeklyWritingUsers());
            maximumUserCount = Math.max(maximumUserCount, rawTrendPoint.getNewlyRegisteredUsers());
            maximumExportCount = Math.max(maximumExportCount, rawTrendPoint.getExportCount());
        }

        return new OperationsTrendScale(
            WeeklyOperationMetricCount.of(maximumUserCount),
            WeeklyOperationMetricCount.of(maximumExportCount)
        );
    }

    long getMaximumUserCount() {
        return mMaximumUserCount.getValue();
    }

    long getMaximumExportCount() {
        return mMaximumExportCount.getValue();
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
}
