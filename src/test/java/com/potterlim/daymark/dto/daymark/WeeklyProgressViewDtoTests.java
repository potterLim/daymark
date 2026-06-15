package com.potterlim.daymark.dto.daymark;

import java.time.LocalDate;
import java.util.List;
import com.potterlim.daymark.support.DaymarkGoalCompletionCounts;
import com.potterlim.daymark.support.DaymarkWeekOffset;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeeklyProgressViewDtoTests {

    @Test
    void builderShouldBuildViewWithTypedWeeklyGoalCounts() {
        WeeklyProgressViewDto weeklyProgressView = WeeklyProgressViewDto.createBuilder(DaymarkWeekOffset.of(0))
            .setWeeklyProgressItems(List.of())
            .setWeeklyGoalCompletionCounts(DaymarkGoalCompletionCounts.of(2, 4))
            .setRangeLabel("2026. 06. 08. ~ 2026. 06. 14.")
            .setCurrentWeekRangeLabel("current")
            .setPreviousWeekRangeLabel("previous")
            .setNextWeekRangeLabel("next")
            .setDefaultDate(LocalDate.of(2026, 6, 14))
            .build();

        assertThat(weeklyProgressView.getWeeklyAchievedGoalCount()).isEqualTo(2);
        assertThat(weeklyProgressView.getWeeklyTotalGoalCount()).isEqualTo(4);
        assertThat(weeklyProgressView.getWeeklyCompletionPercent()).isEqualTo(50);
        assertThat(weeklyProgressView.getWeekOffset()).isZero();
        assertThat(weeklyProgressView.getDefaultDate()).isEqualTo(LocalDate.of(2026, 6, 14));
    }

    @Test
    void buildShouldRequireDefaultDate() {
        WeeklyProgressViewDto.Builder builder = WeeklyProgressViewDto.createBuilder(DaymarkWeekOffset.of(0));

        assertThatThrownBy(builder::build)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("defaultDate must be set.");
    }
}
