package com.potterlim.daymark.dto.daymark;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DaymarkLibraryCalendarDayDtoTests {

    @Test
    void builderShouldBuildCalendarDayWithNamedFlags() {
        DaymarkLibraryCalendarDayDto calendarDay = DaymarkLibraryCalendarDayDto
            .createBuilder(LocalDate.of(2026, 6, 14))
            .markCurrentMonth()
            .markEntryPresent()
            .setCompletionPercent(80)
            .markToday()
            .build();

        assertThat(calendarDay.getDate()).isEqualTo(LocalDate.of(2026, 6, 14));
        assertThat(calendarDay.isCurrentMonth()).isTrue();
        assertThat(calendarDay.hasEntry()).isTrue();
        assertThat(calendarDay.getCompletionPercent()).isEqualTo(80);
        assertThat(calendarDay.getIntensityLevel()).isEqualTo(3);
        assertThat(calendarDay.isToday()).isTrue();
    }

    @Test
    void builderShouldRejectInvalidCompletionPercent() {
        DaymarkLibraryCalendarDayDto.Builder builder = DaymarkLibraryCalendarDayDto
            .createBuilder(LocalDate.of(2026, 6, 14));

        assertThatThrownBy(() -> builder.setCompletionPercent(101))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("completionPercent must be between 0 and 100.");
    }
}
