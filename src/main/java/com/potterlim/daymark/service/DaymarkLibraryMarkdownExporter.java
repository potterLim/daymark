package com.potterlim.daymark.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.potterlim.daymark.dto.daymark.DaymarkLibraryItemDto;
import com.potterlim.daymark.dto.daymark.DaymarkLibrarySearchCriteria;
import org.springframework.stereotype.Component;

@Component
public class DaymarkLibraryMarkdownExporter {

    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy. MM. dd.");

    public String buildMarkdownExport(
        DaymarkLibrarySearchCriteria searchCriteria,
        List<DaymarkLibraryItemDto> matchingItemsAscending
    ) {
        if (searchCriteria == null) {
            throw new IllegalArgumentException("searchCriteria must not be null.");
        }

        if (matchingItemsAscending == null) {
            throw new IllegalArgumentException("matchingItemsAscending must not be null.");
        }

        StringBuilder markdownBuilder = new StringBuilder();
        markdownBuilder.append("# Daymark 기록 라이브러리\r\n\r\n");
        markdownBuilder.append("- 기간: ")
            .append(formatDisplayDate(searchCriteria.getStartDate()))
            .append(" ~ ")
            .append(formatDisplayDate(searchCriteria.getEndDate()))
            .append("\r\n");

        if (searchCriteria.hasKeyword()) {
            markdownBuilder.append("- 검색어: ").append(searchCriteria.getKeywordOrNull()).append("\r\n");
        }

        markdownBuilder.append("- 기록: ").append(matchingItemsAscending.size()).append("일\r\n");

        if (matchingItemsAscending.isEmpty()) {
            markdownBuilder.append("\r\n선택한 조건에 해당하는 기록이 없습니다.");
            return markdownBuilder.toString();
        }

        for (DaymarkLibraryItemDto libraryItem : matchingItemsAscending) {
            markdownBuilder.append("\r\n\r\n---\r\n\r\n");
            markdownBuilder.append("## ").append(formatDisplayDate(libraryItem.getDate())).append("\r\n\r\n");
            markdownBuilder.append("- 흐름: ").append(libraryItem.getFlowLabel()).append("\r\n");
            markdownBuilder.append("- 완료율: ")
                .append(libraryItem.getCompletionPercent())
                .append("% (")
                .append(libraryItem.getAchievedGoalCount())
                .append(" / ")
                .append(libraryItem.getTotalGoalCount())
                .append(")\r\n\r\n");
            markdownBuilder.append(libraryItem.getMarkdownText());
        }

        return markdownBuilder.toString().stripTrailing();
    }

    private static String formatDisplayDate(LocalDate date) {
        return DISPLAY_DATE_FORMATTER.format(date);
    }
}
