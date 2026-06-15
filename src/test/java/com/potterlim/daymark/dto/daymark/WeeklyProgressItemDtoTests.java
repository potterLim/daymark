package com.potterlim.daymark.dto.daymark;

import java.time.LocalDate;
import com.potterlim.daymark.support.DaymarkGoalCompletionCounts;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeeklyProgressItemDtoTests {

    @Test
    void fromGoalCompletionCountsShouldCreateItemFromTypedCounts() {
        WeeklyProgressItemDto weeklyProgressItem = WeeklyProgressItemDto.fromGoalCompletionCounts(
            LocalDate.of(2026, 6, 14),
            DaymarkGoalCompletionCounts.of(1, 2)
        );

        assertThat(weeklyProgressItem.getDate()).isEqualTo(LocalDate.of(2026, 6, 14));
        assertThat(weeklyProgressItem.getAchievedGoalCount()).isEqualTo(1);
        assertThat(weeklyProgressItem.getTotalGoalCount()).isEqualTo(2);
        assertThat(weeklyProgressItem.getCompletionPercent()).isEqualTo(50);
    }

    @Test
    void fromGoalCompletionCountsShouldRejectNullCounts() {
        assertThatThrownBy(() -> WeeklyProgressItemDto.fromGoalCompletionCounts(
            LocalDate.of(2026, 6, 14),
            null
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("goalCompletionCounts must not be null.");
    }
}
