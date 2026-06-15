package com.potterlim.daymark.dto.daymark;

import java.time.LocalDate;
import java.util.List;
import com.potterlim.daymark.support.DaymarkGoalCompletionCounts;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DaymarkLibraryViewDtoTests {

    @Test
    void builderShouldBuildViewWithTypedGoalCounts() {
        DaymarkLibrarySearchCriteria searchCriteria = DaymarkLibrarySearchCriteria.create(
            null,
            null,
            null,
            LocalDate.of(2026, 6, 14)
        );

        DaymarkLibraryViewDto libraryView = DaymarkLibraryViewDto.createBuilder(searchCriteria)
            .setItems(List.of())
            .setTrendItems(List.of())
            .setCalendarDays(List.of())
            .setCalendarMonthDate(LocalDate.of(2026, 6, 1))
            .setMorningEntryCount(2)
            .setEveningEntryCount(1)
            .setGoalCompletionCounts(DaymarkGoalCompletionCounts.of(1, 2))
            .build();

        assertThat(libraryView.getCalendarMonthDate()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(libraryView.getMorningEntryCount()).isEqualTo(2);
        assertThat(libraryView.getEveningEntryCount()).isEqualTo(1);
        assertThat(libraryView.getAchievedGoalCount()).isEqualTo(1);
        assertThat(libraryView.getTotalGoalCount()).isEqualTo(2);
        assertThat(libraryView.getAverageGoalCompletionPercent()).isEqualTo(50);
    }

    @Test
    void buildShouldRequireCalendarMonthDate() {
        DaymarkLibrarySearchCriteria searchCriteria = DaymarkLibrarySearchCriteria.create(
            null,
            null,
            null,
            LocalDate.of(2026, 6, 14)
        );

        DaymarkLibraryViewDto.Builder builder = DaymarkLibraryViewDto.createBuilder(searchCriteria);

        assertThatThrownBy(builder::build)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("calendarMonthDate must be set.");
    }
}
