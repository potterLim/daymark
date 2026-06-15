package com.potterlim.daymark.dto.operations;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.ToDoubleFunction;
import com.potterlim.daymark.entity.WeeklyOperationMetricSnapshot;
import com.potterlim.daymark.support.WeeklyOperationsSummary;

public final class OperationsTrendViewAssembler {

    private static final int MAXIMUM_TREND_POINT_COUNT = 24;

    private OperationsTrendViewAssembler() {
    }

    public static OperationsTrendViewDto create(
        List<WeeklyOperationMetricSnapshot> recentWeeklySnapshots,
        WeeklyOperationsSummary currentWeeklySummary
    ) {
        if (recentWeeklySnapshots == null) {
            throw new IllegalArgumentException("recentWeeklySnapshots must not be null.");
        }

        if (currentWeeklySummary == null) {
            throw new IllegalArgumentException("currentWeeklySummary must not be null.");
        }

        List<OperationsTrendRawPointDto> rawTrendPoints = createRawTrendPoints(
            recentWeeklySnapshots,
            currentWeeklySummary
        );
        OperationsTrendScale operationsTrendScale = OperationsTrendScale.create(rawTrendPoints);
        List<OperationsTrendPointDto> trendPoints = createTrendPoints(rawTrendPoints, operationsTrendScale);

        OperationsTrendPointDto latestTrendPoint = trendPoints.get(trendPoints.size() - 1);
        OperationsTrendPointDto previousTrendPointOrNull = null;
        if (trendPoints.size() > 1) {
            previousTrendPointOrNull = trendPoints.get(trendPoints.size() - 2);
        }

        return new OperationsTrendViewDto(
            trendPoints,
            buildLinePoints(trendPoints, OperationsTrendPointDto::getActiveUserYAxisCoordinate),
            buildLinePoints(trendPoints, OperationsTrendPointDto::getWritingUserYAxisCoordinate),
            buildLinePoints(trendPoints, OperationsTrendPointDto::getNewUserYAxisCoordinate),
            buildLinePoints(trendPoints, OperationsTrendPointDto::getGoalCompletionYAxisCoordinate),
            formatLongDelta(
                latestTrendPoint.getWeeklyActiveUsers(),
                readPreviousWeeklyActiveUsersOrNull(previousTrendPointOrNull)
            ),
            formatLongDelta(
                latestTrendPoint.getWeeklyWritingUsers(),
                readPreviousWeeklyWritingUsersOrNull(previousTrendPointOrNull)
            ),
            formatLongDelta(
                latestTrendPoint.getExportCount(),
                readPreviousExportCountOrNull(previousTrendPointOrNull)
            ),
            formatPercentPointDelta(
                latestTrendPoint.getGoalCompletionRatePercent(),
                readPreviousGoalCompletionRatePercentOrNull(previousTrendPointOrNull)
            ),
            previousTrendPointOrNull != null
        );
    }

    private static List<OperationsTrendRawPointDto> createRawTrendPoints(
        List<WeeklyOperationMetricSnapshot> recentWeeklySnapshots,
        WeeklyOperationsSummary currentWeeklySummary
    ) {
        List<OperationsTrendRawPointDto> rawTrendPoints = new ArrayList<>();
        for (WeeklyOperationMetricSnapshot weeklyOperationMetricSnapshot : recentWeeklySnapshots) {
            boolean isCurrentWeekSnapshot = weeklyOperationMetricSnapshot.getWeekStartDate()
                .equals(currentWeeklySummary.getWeekStartDate());

            if (!isCurrentWeekSnapshot) {
                rawTrendPoints.add(
                    OperationsTrendRawPointDto.createFromSnapshot(weeklyOperationMetricSnapshot)
                );
            }
        }

        rawTrendPoints.add(OperationsTrendRawPointDto.createFromSummary(currentWeeklySummary));
        rawTrendPoints.sort(Comparator.comparing(OperationsTrendRawPointDto::getWeekStartDate));

        if (rawTrendPoints.size() <= MAXIMUM_TREND_POINT_COUNT) {
            return rawTrendPoints;
        }

        return new ArrayList<>(rawTrendPoints.subList(
            rawTrendPoints.size() - MAXIMUM_TREND_POINT_COUNT,
            rawTrendPoints.size()
        ));
    }

    private static List<OperationsTrendPointDto> createTrendPoints(
        List<OperationsTrendRawPointDto> rawTrendPoints,
        OperationsTrendScale operationsTrendScale
    ) {
        List<OperationsTrendPointDto> trendPoints = new ArrayList<>();
        for (int pointIndex = 0; pointIndex < rawTrendPoints.size(); pointIndex += 1) {
            trendPoints.add(new OperationsTrendPointDto(
                rawTrendPoints.get(pointIndex),
                pointIndex,
                rawTrendPoints.size(),
                operationsTrendScale
            ));
        }

        return trendPoints;
    }

    private static String buildLinePoints(
        List<OperationsTrendPointDto> trendPoints,
        ToDoubleFunction<OperationsTrendPointDto> yAxisCoordinateReader
    ) {
        List<String> linePoints = new ArrayList<>();
        for (OperationsTrendPointDto trendPoint : trendPoints) {
            linePoints.add(String.format(
                Locale.US,
                "%.1f,%.1f",
                trendPoint.getXAxisCoordinate(),
                yAxisCoordinateReader.applyAsDouble(trendPoint)
            ));
        }

        return String.join(" ", linePoints);
    }

    private static String formatLongDelta(long currentValue, Long previousValueOrNull) {
        if (previousValueOrNull == null) {
            return "-";
        }

        long delta = currentValue - previousValueOrNull;
        if (delta >= 0L) {
            return "+" + delta;
        }

        return String.valueOf(delta);
    }

    private static String formatPercentPointDelta(double currentValue, Double previousValueOrNull) {
        if (previousValueOrNull == null) {
            return "-";
        }

        double delta = currentValue - previousValueOrNull;
        return String.format(Locale.US, "%+.1fp", delta);
    }

    private static Long readPreviousWeeklyActiveUsersOrNull(OperationsTrendPointDto previousTrendPointOrNull) {
        if (previousTrendPointOrNull == null) {
            return null;
        }

        return previousTrendPointOrNull.getWeeklyActiveUsers();
    }

    private static Long readPreviousWeeklyWritingUsersOrNull(OperationsTrendPointDto previousTrendPointOrNull) {
        if (previousTrendPointOrNull == null) {
            return null;
        }

        return previousTrendPointOrNull.getWeeklyWritingUsers();
    }

    private static Long readPreviousExportCountOrNull(OperationsTrendPointDto previousTrendPointOrNull) {
        if (previousTrendPointOrNull == null) {
            return null;
        }

        return previousTrendPointOrNull.getExportCount();
    }

    private static Double readPreviousGoalCompletionRatePercentOrNull(
        OperationsTrendPointDto previousTrendPointOrNull
    ) {
        if (previousTrendPointOrNull == null) {
            return null;
        }

        return previousTrendPointOrNull.getGoalCompletionRatePercent();
    }
}
