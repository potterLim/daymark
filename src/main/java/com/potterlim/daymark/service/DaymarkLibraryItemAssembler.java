package com.potterlim.daymark.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringJoiner;
import com.potterlim.daymark.dto.daymark.DaymarkLibraryGoalPreviewDto;
import com.potterlim.daymark.dto.daymark.DaymarkLibraryItemDto;
import com.potterlim.daymark.dto.daymark.DaymarkLibrarySearchCriteria;
import com.potterlim.daymark.entity.DaymarkEntry;
import com.potterlim.daymark.support.DaymarkGoalCompletionCounts;
import com.potterlim.daymark.support.EDaymarkSectionType;
import org.springframework.stereotype.Component;

@Component
public class DaymarkLibraryItemAssembler {

    private static final int MAX_GOAL_PREVIEW_COUNT = 3;
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy. MM. dd.");
    private static final List<EDaymarkSectionType> EXCERPT_SECTION_TYPES = List.of(
        EDaymarkSectionType.ACHIEVEMENTS,
        EDaymarkSectionType.IMPROVEMENTS,
        EDaymarkSectionType.NOTES,
        EDaymarkSectionType.FOCUS,
        EDaymarkSectionType.GOALS
    );

    public DaymarkLibraryItemDto buildItem(DaymarkEntry daymarkEntry) {
        if (daymarkEntry == null) {
            throw new IllegalArgumentException("daymarkEntry must not be null.");
        }

        List<String> goalLines = DaymarkLibraryItemText.splitContentLines(
            daymarkEntry.readSection(EDaymarkSectionType.GOALS)
        );
        Set<String> completedGoalTexts = buildCompletedGoalTextSet(daymarkEntry);
        DaymarkGoalCompletionCounts goalCompletionCounts = DaymarkGoalCompletionCounts.of(
            countAchievedGoals(goalLines, completedGoalTexts),
            goalLines.size()
        );
        DaymarkLibraryItemDto.Builder libraryItemBuilder = DaymarkLibraryItemDto.createBuilder(daymarkEntry.getEntryDate())
            .setAchievedGoalCount(goalCompletionCounts.getCompletedGoalCount())
            .setTotalGoalCount(goalCompletionCounts.getTotalGoalCount())
            .setCompletionPercent(goalCompletionCounts.calculateCompletionPercent())
            .setExcerpt(buildExcerpt(daymarkEntry))
            .setMarkdownText(DaymarkEntryMarkdownFormatter.format(daymarkEntry))
            .setGoalPreviewItems(buildGoalPreviewItems(goalLines, completedGoalTexts))
            .setHiddenGoalCount(Math.max(0, goalLines.size() - MAX_GOAL_PREVIEW_COUNT))
            .setContentBlocks(DaymarkLibraryContentBlockAssembler.buildContentBlocks(daymarkEntry));

        if (daymarkEntry.hasMorningEntry()) {
            libraryItemBuilder.markMorningEntryPresent();
        }

        if (daymarkEntry.hasEveningEntry()) {
            libraryItemBuilder.markEveningEntryPresent();
        }

        return libraryItemBuilder.build();
    }

    public boolean matchesSearchCriteria(
        DaymarkLibraryItemDto libraryItem,
        DaymarkLibrarySearchCriteria searchCriteria
    ) {
        if (libraryItem == null) {
            throw new IllegalArgumentException("libraryItem must not be null.");
        }

        if (searchCriteria == null) {
            throw new IllegalArgumentException("searchCriteria must not be null.");
        }

        if (!searchCriteria.hasKeyword()) {
            return true;
        }

        String normalizedKeyword = searchCriteria.getKeywordOrNull().toLowerCase(Locale.ROOT);
        return buildSearchableText(libraryItem).contains(normalizedKeyword);
    }

    private static String buildSearchableText(DaymarkLibraryItemDto libraryItem) {
        return new StringJoiner(" ")
            .add(libraryItem.getDate().toString())
            .add(formatDisplayDate(libraryItem.getDate()))
            .add(libraryItem.getFlowLabel())
            .add(libraryItem.getExcerpt())
            .add(libraryItem.getMarkdownText())
            .toString()
            .toLowerCase(Locale.ROOT);
    }

    private static String buildExcerpt(DaymarkEntry daymarkEntry) {
        for (EDaymarkSectionType daymarkSectionType : EXCERPT_SECTION_TYPES) {
            List<String> contentLines = DaymarkLibraryItemText.splitContentLines(
                daymarkEntry.readSection(daymarkSectionType)
            );
            if (!contentLines.isEmpty()) {
                return DaymarkLibraryItemText.abbreviate(contentLines.get(0));
            }
        }

        return "기록 내용이 저장되었습니다.";
    }

    private static Set<String> buildCompletedGoalTextSet(DaymarkEntry daymarkEntry) {
        Set<String> completedGoalTexts = new HashSet<>();
        for (String checkedGoalText : daymarkEntry.readCheckedGoalTexts()) {
            completedGoalTexts.add(DaymarkLibraryItemText.normalizeComparableGoalText(checkedGoalText));
        }

        return completedGoalTexts;
    }

    private static List<DaymarkLibraryGoalPreviewDto> buildGoalPreviewItems(
        List<String> goalLines,
        Set<String> completedGoalTexts
    ) {
        List<DaymarkLibraryGoalPreviewDto> goalPreviewItems = new ArrayList<>();
        int visibleGoalCount = Math.min(goalLines.size(), MAX_GOAL_PREVIEW_COUNT);
        for (int goalIndex = 0; goalIndex < visibleGoalCount; goalIndex++) {
            String goalLine = goalLines.get(goalIndex);
            String abbreviatedGoalLine = DaymarkLibraryItemText.abbreviate(goalLine);
            if (completedGoalTexts.contains(DaymarkLibraryItemText.normalizeComparableGoalText(goalLine))) {
                goalPreviewItems.add(DaymarkLibraryGoalPreviewDto.createCompleted(abbreviatedGoalLine));
            } else {
                goalPreviewItems.add(DaymarkLibraryGoalPreviewDto.createPending(abbreviatedGoalLine));
            }
        }

        return goalPreviewItems;
    }

    private static int countAchievedGoals(List<String> goalLines, Set<String> completedGoalTexts) {
        int achievedGoalCount = 0;
        for (String goalLine : goalLines) {
            if (completedGoalTexts.contains(DaymarkLibraryItemText.normalizeComparableGoalText(goalLine))) {
                achievedGoalCount++;
            }
        }

        return achievedGoalCount;
    }

    private static String formatDisplayDate(LocalDate date) {
        return DISPLAY_DATE_FORMATTER.format(date);
    }
}
