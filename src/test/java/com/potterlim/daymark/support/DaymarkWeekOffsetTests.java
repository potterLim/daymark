package com.potterlim.daymark.support;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DaymarkWeekOffsetTests {

    @Test
    void calculateReferenceDateFromShouldMoveByFullWeeks() {
        DaymarkWeekOffset weekOffset = DaymarkWeekOffset.of(-2);

        LocalDate referenceDate = weekOffset.calculateReferenceDateFrom(LocalDate.of(2026, 6, 14));

        assertThat(referenceDate).isEqualTo(LocalDate.of(2026, 5, 31));
    }

    @Test
    void previousAndNextShouldPreserveWeekOffsetMeaning() {
        DaymarkWeekOffset weekOffset = DaymarkWeekOffset.of(3);

        assertThat(weekOffset.previous().getValue()).isEqualTo(2);
        assertThat(weekOffset.next().getValue()).isEqualTo(4);
    }

    @Test
    void calculateReferenceDateFromShouldRejectNullCurrentDate() {
        DaymarkWeekOffset weekOffset = DaymarkWeekOffset.of(0);

        assertThatThrownBy(() -> weekOffset.calculateReferenceDateFrom(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("currentDate must not be null.");
    }
}
