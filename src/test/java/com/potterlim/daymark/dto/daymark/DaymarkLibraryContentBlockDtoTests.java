package com.potterlim.daymark.dto.daymark;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DaymarkLibraryContentBlockDtoTests {

    @Test
    void builderShouldBuildContentBlockWithNamedFields() {
        DaymarkLibraryContentBlockDto contentBlock = DaymarkLibraryContentBlockDto
            .createBuilder(EDaymarkLibraryContentTone.FOCUS)
            .setEyebrow("Focus")
            .setTitle("집중 영역")
            .setLines(List.of("billing flow"))
            .setHiddenLineCount(1)
            .build();

        assertThat(contentBlock.getEyebrow()).isEqualTo("Focus");
        assertThat(contentBlock.getTitle()).isEqualTo("집중 영역");
        assertThat(contentBlock.getLines()).containsExactly("billing flow");
        assertThat(contentBlock.hasHiddenLines()).isTrue();
        assertThat(contentBlock.getToneCssClass()).isEqualTo("library-snapshot-card-focus");
    }

    @Test
    void builderShouldRejectInvalidHiddenLineCount() {
        DaymarkLibraryContentBlockDto.Builder builder = DaymarkLibraryContentBlockDto
            .createBuilder(EDaymarkLibraryContentTone.FOCUS);

        assertThatThrownBy(() -> builder.setHiddenLineCount(-1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("hiddenLineCount must not be negative.");
    }
}
