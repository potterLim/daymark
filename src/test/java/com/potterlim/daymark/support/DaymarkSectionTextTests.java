package com.potterlim.daymark.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DaymarkSectionTextTests {

    @Test
    void createShouldTreatNullAsEmptyText() {
        DaymarkSectionText sectionText = DaymarkSectionText.create(null);

        assertThat(sectionText.getValue()).isEmpty();
        assertThat(sectionText.isBlank()).isTrue();
    }

    @Test
    void normalizeSectionBodyShouldTrimTrailingSpaceAndBlankEdges() {
        DaymarkSectionText sectionText = DaymarkSectionText.create("  first  \r\n\r\nsecond   ");

        assertThat(sectionText.normalizeSectionBody().getValue()).isEqualTo("first\r\nsecond");
    }
}
