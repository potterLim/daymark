package com.potterlim.daymark.support;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DaymarkEntryDateTests {

    @Test
    void ofShouldRejectNullValue() {
        assertThatThrownBy(() -> DaymarkEntryDate.of(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("value must not be null.");
    }

    @Test
    void containingWeekRangeShouldUseMondayStart() {
        DaymarkEntryDate entryDate = DaymarkEntryDate.of(LocalDate.of(2026, 6, 14));

        DaymarkWeekRange weekRange = entryDate.containingWeekRange();

        assertThat(weekRange.getStartDate()).isEqualTo(LocalDate.of(2026, 6, 8));
        assertThat(weekRange.getEndDate()).isEqualTo(LocalDate.of(2026, 6, 14));
    }
}
