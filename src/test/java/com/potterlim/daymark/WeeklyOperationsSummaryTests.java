package com.potterlim.daymark;

import java.time.LocalDate;
import com.potterlim.daymark.support.DaymarkWeekRange;
import com.potterlim.daymark.support.WeeklyOperationMetricAverage;
import com.potterlim.daymark.support.WeeklyOperationMetricCount;
import com.potterlim.daymark.support.WeeklyOperationMetricPercent;
import com.potterlim.daymark.support.WeeklyOperationsSummary;
import com.potterlim.daymark.support.WeeklyOperationsSummaryBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeeklyOperationsSummaryTests {

    @Test
    void builderShouldBuildSummaryWithNamedMetrics() {
        WeeklyOperationsSummary weeklyOperationsSummary = WeeklyOperationsSummary.createBuilder(
                DaymarkWeekRange.of(LocalDate.of(2026, 6, 8), LocalDate.of(2026, 6, 14))
            )
            .setTotalRegisteredUsers(WeeklyOperationMetricCount.of(3L))
            .setNewlyRegisteredUsers(WeeklyOperationMetricCount.of(1L))
            .setWeeklyActiveUsers(WeeklyOperationMetricCount.of(2L))
            .setWeeklyWritingUsers(WeeklyOperationMetricCount.of(1L))
            .setWeeklyWritingDays(WeeklyOperationMetricCount.of(4L))
            .setWeeklyMorningEntries(WeeklyOperationMetricCount.of(3L))
            .setWeeklyEveningEntries(WeeklyOperationMetricCount.of(2L))
            .setWeeklyPlanReviewCompletedDays(WeeklyOperationMetricCount.of(1L))
            .setSignInSucceededCount(WeeklyOperationMetricCount.of(5L))
            .setSignInFailedCount(WeeklyOperationMetricCount.of(1L))
            .setWeeklyReviewViewedCount(WeeklyOperationMetricCount.of(2L))
            .setRecordLibraryViewedCount(WeeklyOperationMetricCount.of(2L))
            .setMarkdownExportedCount(WeeklyOperationMetricCount.of(1L))
            .setPdfExportViewedCount(WeeklyOperationMetricCount.of(1L))
            .setExportingUsers(WeeklyOperationMetricCount.of(1L))
            .setNewWorkspaceActivatedUsers(WeeklyOperationMetricCount.of(1L))
            .setAverageWritingDaysPerActiveUser(WeeklyOperationMetricAverage.of(2.0))
            .setAverageEntryCompletionsPerActiveUser(WeeklyOperationMetricAverage.of(2.5))
            .setPlanReviewConversionRatePercent(WeeklyOperationMetricPercent.of(50.0))
            .setNewWorkspaceActivationRatePercent(WeeklyOperationMetricPercent.of(100.0))
            .setGoalCompletionRatePercent(WeeklyOperationMetricPercent.of(75.0))
            .build();

        assertThat(weeklyOperationsSummary.getWeekStartDate()).isEqualTo(LocalDate.of(2026, 6, 8));
        assertThat(weeklyOperationsSummary.getExportCount()).isEqualTo(2L);
        assertThat(weeklyOperationsSummary.getGoalCompletionRatePercent()).isEqualTo(75.0);
    }

    @Test
    void builderShouldRejectInvalidMetrics() {
        WeeklyOperationsSummaryBuilder builder = WeeklyOperationsSummary.createBuilder(
            DaymarkWeekRange.of(LocalDate.of(2026, 6, 8), LocalDate.of(2026, 6, 14))
        );

        assertThatThrownBy(() -> builder.setTotalRegisteredUsers(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("totalRegisteredUsers must not be null.");

        assertThatThrownBy(() -> WeeklyOperationMetricCount.of(-1L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("weeklyOperationMetricCount must not be negative.");

        assertThatThrownBy(() -> WeeklyOperationMetricPercent.of(101.0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("weeklyOperationMetricPercent must be between 0 and 100.");
    }
}
