package com.potterlim.daymark.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DaymarkPreviewMarkdownTests {

    @Test
    void createShouldTreatBlankTextAsEmptyMarkdown() {
        DaymarkPreviewMarkdown previewMarkdown = DaymarkPreviewMarkdown.create("   ");

        assertThat(previewMarkdown.isBlank()).isTrue();
        assertThat(previewMarkdown.getValue()).isEmpty();
    }

    @Test
    void fromMorningSectionsShouldBuildPreviewMarkdownWithSectionHeaders() {
        DaymarkPreviewMarkdown previewMarkdown = DaymarkPreviewMarkdown.fromMorningSections(
            DaymarkSectionText.create("ship release\nreview metrics"),
            DaymarkSectionText.create("billing flow"),
            DaymarkSectionText.empty()
        );

        assertThat(previewMarkdown.getValue())
            .isEqualTo("## 오늘의 목표\r\n\r\n- ship release\r\n- review metrics\r\n\r\n## 집중 영역\r\n\r\n- billing flow");
    }

    @Test
    void fromMorningSectionsShouldRejectNullSection() {
        assertThatThrownBy(() -> DaymarkPreviewMarkdown.fromMorningSections(
            null,
            DaymarkSectionText.empty(),
            DaymarkSectionText.empty()
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("goals must not be null.");
    }

    @Test
    void normalizeForRenderingShouldShiftHeaderLevels() {
        DaymarkPreviewMarkdown previewMarkdown = DaymarkPreviewMarkdown.create("# Goals\r\n- first");

        DaymarkPreviewMarkdown normalizedPreviewMarkdown = previewMarkdown.normalizeForRendering();

        assertThat(normalizedPreviewMarkdown.getValue()).isEqualTo("### Goals\r\n- first");
    }
}
