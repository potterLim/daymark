package com.potterlim.daymark.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DaymarkGoalCompletionCountsTests {

    @Test
    void calculateCompletionPercentShouldReturnWholePercent() {
        DaymarkGoalCompletionCounts goalCompletionCounts = DaymarkGoalCompletionCounts.of(2, 3);

        assertThat(goalCompletionCounts.calculateCompletionPercent()).isEqualTo(66);
        assertThat(goalCompletionCounts.calculateCompletionRatePercent()).isEqualTo(66.66666666666667);
    }

    @Test
    void emptyShouldRepresentNoTrackedGoals() {
        DaymarkGoalCompletionCounts goalCompletionCounts = DaymarkGoalCompletionCounts.empty();

        assertThat(goalCompletionCounts.getCompletedGoalCount()).isZero();
        assertThat(goalCompletionCounts.getTotalGoalCount()).isZero();
        assertThat(goalCompletionCounts.calculateCompletionPercent()).isZero();
        assertThat(goalCompletionCounts.calculateCompletionRatePercent()).isZero();
    }

    @Test
    void plusShouldAddGoalCounts() {
        DaymarkGoalCompletionCounts goalCompletionCounts = DaymarkGoalCompletionCounts.of(1, 3)
            .plus(DaymarkGoalCompletionCounts.of(2, 4));

        assertThat(goalCompletionCounts.getCompletedGoalCount()).isEqualTo(3);
        assertThat(goalCompletionCounts.getTotalGoalCount()).isEqualTo(7);
    }

    @Test
    void ofShouldRejectInvalidCounts() {
        assertThatThrownBy(() -> DaymarkGoalCompletionCounts.of(-1, 1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("completedGoalCount must not be negative.");

        assertThatThrownBy(() -> DaymarkGoalCompletionCounts.of(1, -1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("totalGoalCount must not be negative.");

        assertThatThrownBy(() -> DaymarkGoalCompletionCounts.of(2, 1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("completedGoalCount must not exceed totalGoalCount.");
    }
}
