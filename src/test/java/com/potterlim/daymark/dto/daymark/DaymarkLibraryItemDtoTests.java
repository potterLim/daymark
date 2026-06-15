package com.potterlim.daymark.dto.daymark;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DaymarkLibraryItemDtoTests {

    @Test
    void builderShouldBuildItemWithNamedFields() {
        DaymarkLibraryItemDto libraryItem = DaymarkLibraryItemDto.createBuilder(LocalDate.of(2026, 6, 14))
            .markMorningEntryPresent()
            .markEveningEntryPresent()
            .setAchievedGoalCount(1)
            .setTotalGoalCount(2)
            .setCompletionPercent(50)
            .setExcerpt("excerpt")
            .setMarkdownText("## 기록")
            .setGoalPreviewItems(List.of(DaymarkLibraryGoalPreviewDto.createCompleted("ship release")))
            .setHiddenGoalCount(1)
            .setContentBlocks(List.of())
            .build();

        assertThat(libraryItem.getDate()).isEqualTo(LocalDate.of(2026, 6, 14));
        assertThat(libraryItem.hasMorningEntry()).isTrue();
        assertThat(libraryItem.hasEveningEntry()).isTrue();
        assertThat(libraryItem.getCompletionPercent()).isEqualTo(50);
        assertThat(libraryItem.getFlowLabel()).isEqualTo("계획과 회고");
        assertThat(libraryItem.hasHiddenGoals()).isTrue();
    }

    @Test
    void builderShouldRejectInvalidCounts() {
        DaymarkLibraryItemDto.Builder builder = DaymarkLibraryItemDto.createBuilder(LocalDate.of(2026, 6, 14));

        assertThatThrownBy(() -> builder.setAchievedGoalCount(-1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("achievedGoalCount must not be negative.");

        assertThatThrownBy(() -> builder.setCompletionPercent(101))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("completionPercent must be between 0 and 100.");
    }

    @Test
    void buildShouldRejectGoalCountMismatch() {
        DaymarkLibraryItemDto.Builder builder = DaymarkLibraryItemDto.createBuilder(LocalDate.of(2026, 6, 14))
            .setAchievedGoalCount(2)
            .setTotalGoalCount(1);

        assertThatThrownBy(builder::build)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("achievedGoalCount must not be greater than totalGoalCount.");
    }
}
