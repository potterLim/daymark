package com.potterlim.daymark.support;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public final class DaymarkGoalMarkdown {

    private static final String CHECKED_GOAL_PREFIX_LOWERCASE = "- [x] ";
    private static final String CHECKED_GOAL_PREFIX_UPPERCASE = "- [X] ";
    private static final String PENDING_GOAL_PREFIX = "- [ ] ";

    private DaymarkGoalMarkdown() {
    }

    public static String buildGoalList(String goalsOrNull) {
        if (goalsOrNull == null || goalsOrNull.isBlank()) {
            return "";
        }

        StringJoiner stringJoiner = new StringJoiner("\r\n");
        for (String goalLine : DaymarkTextLines.splitNonBlankTrimmedLines(goalsOrNull)) {
            stringJoiner.add("- " + goalLine);
        }

        return stringJoiner.toString();
    }

    public static String buildCheckedGoalList(List<DaymarkGoalCheckItem> goalItemsOrNull) {
        if (goalItemsOrNull == null || goalItemsOrNull.isEmpty()) {
            return "";
        }

        StringJoiner stringJoiner = new StringJoiner("\r\n");
        for (DaymarkGoalCheckItem goalItem : goalItemsOrNull) {
            if (goalItem == null || !goalItem.hasText()) {
                continue;
            }

            String checkedMarker = " ";
            if (goalItem.isDone()) {
                checkedMarker = "x";
            }

            stringJoiner.add("- [" + checkedMarker + "] " + goalItem.getText());
        }

        return stringJoiner.toString();
    }

    public static List<String> readCheckedGoalTexts(String checklistMarkdownOrNull) {
        List<String> checkedGoalTexts = new ArrayList<>();
        for (String line : DaymarkTextLines.splitLines(checklistMarkdownOrNull)) {
            if (isCheckedGoalLine(line)) {
                checkedGoalTexts.add(line.substring(CHECKED_GOAL_PREFIX_LOWERCASE.length()).trim());
            }
        }

        return checkedGoalTexts;
    }

    public static DaymarkGoalCompletionCounts countGoalCompletion(String checklistMarkdownOrNull) {
        int completedGoalCount = 0;
        int totalGoalCount = 0;
        for (String line : DaymarkTextLines.splitLines(checklistMarkdownOrNull)) {
            String trimmedLine = line.trim();
            if (isCheckedGoalLine(trimmedLine)) {
                completedGoalCount += 1;
                totalGoalCount += 1;
                continue;
            }

            if (isPendingGoalLine(trimmedLine)) {
                totalGoalCount += 1;
            }
        }

        return DaymarkGoalCompletionCounts.of(completedGoalCount, totalGoalCount);
    }

    public static boolean isCheckedGoalLine(String line) {
        return line.startsWith(CHECKED_GOAL_PREFIX_LOWERCASE)
            || line.startsWith(CHECKED_GOAL_PREFIX_UPPERCASE);
    }

    public static boolean isPendingGoalLine(String line) {
        return line.startsWith(PENDING_GOAL_PREFIX);
    }
}
