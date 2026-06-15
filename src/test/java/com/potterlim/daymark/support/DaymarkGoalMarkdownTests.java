package com.potterlim.daymark.support;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DaymarkGoalMarkdownTests {

    @Test
    void buildGoalListShouldNormalizeNonBlankLines() {
        String goalList = DaymarkGoalMarkdown.buildGoalList(" first goal \r\n\r\nsecond goal\n ");

        assertThat(goalList).isEqualTo("- first goal\r\n- second goal");
    }

    @Test
    void buildCheckedGoalListShouldKeepDoneStateAndIgnoreEmptyItems() {
        String checkedGoalList = DaymarkGoalMarkdown.buildCheckedGoalList(List.of(
            DaymarkGoalCheckItem.createCompleted(" first goal "),
            DaymarkGoalCheckItem.createPending("second goal"),
            DaymarkGoalCheckItem.createCompleted(" ")
        ));

        assertThat(checkedGoalList).isEqualTo("- [x] first goal\r\n- [ ] second goal");
    }

    @Test
    void readCheckedGoalTextsShouldOnlyAcceptGeneratedChecklistFormat() {
        List<String> checkedGoalTexts = DaymarkGoalMarkdown.readCheckedGoalTexts(String.join("\r\n",
            "- [x] shipped",
            "- [X] reviewed",
            "- [x]missing-space",
            "- [ ] pending"
        ));

        assertThat(checkedGoalTexts).containsExactly("shipped", "reviewed");
    }

    @Test
    void countGoalCompletionShouldOnlyCountChecklistItems() {
        DaymarkGoalCompletionCounts goalCompletionCounts =
            DaymarkGoalMarkdown.countGoalCompletion(String.join("\r\n",
                "- [x] shipped",
                "- [ ] pending",
                "- [X] reviewed",
                "- [ ]missing-space",
                "- plain note"
            ));

        assertThat(goalCompletionCounts.getTotalGoalCount()).isEqualTo(3);
        assertThat(goalCompletionCounts.getCompletedGoalCount()).isEqualTo(2);
    }
}
