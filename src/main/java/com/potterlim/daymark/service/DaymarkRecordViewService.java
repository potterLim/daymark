package com.potterlim.daymark.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import com.potterlim.daymark.dto.daymark.DaymarkLibraryItemDto;
import com.potterlim.daymark.dto.daymark.EveningFormDto;
import com.potterlim.daymark.dto.daymark.EveningGoalItemDto;
import com.potterlim.daymark.entity.UserAccountId;
import com.potterlim.daymark.support.EDaymarkSectionType;
import com.potterlim.daymark.support.SimpleMarkdownRenderer;
import org.springframework.stereotype.Service;

@Service
public class DaymarkRecordViewService {

    private static final String EMPTY_MORNING_LOG_HTML = "<p><em>아침 계획이 없습니다.</em></p>";

    private final IDaymarkService mDaymarkService;
    private final SimpleMarkdownRenderer mSimpleMarkdownRenderer;

    public DaymarkRecordViewService(
        IDaymarkService daymarkService,
        SimpleMarkdownRenderer simpleMarkdownRenderer
    ) {
        mDaymarkService = daymarkService;
        mSimpleMarkdownRenderer = simpleMarkdownRenderer;
    }

    public String buildGoalMarkdownList(String goalsOrNull) {
        if (goalsOrNull == null || goalsOrNull.isBlank()) {
            return "";
        }

        return goalsOrNull.lines()
            .map(String::trim)
            .filter(goalLine -> !goalLine.isEmpty())
            .map(goalLine -> "- " + goalLine)
            .collect(Collectors.joining("\r\n"));
    }

    public String buildCheckedGoalMarkdownList(List<EveningGoalItemDto> goalItemsOrNull) {
        if (goalItemsOrNull == null || goalItemsOrNull.isEmpty()) {
            return "";
        }

        StringJoiner stringJoiner = new StringJoiner("\r\n");
        for (EveningGoalItemDto eveningGoalItemDto : goalItemsOrNull) {
            String goalText = "";
            if (eveningGoalItemDto.getText() != null) {
                goalText = eveningGoalItemDto.getText().trim();
            }

            if (goalText.isEmpty()) {
                continue;
            }

            String checkedMarker = " ";
            if (eveningGoalItemDto.isDone()) {
                checkedMarker = "x";
            }
            stringJoiner.add("- [" + checkedMarker + "] " + goalText);
        }

        return stringJoiner.toString();
    }

    /**
     * Rebuilds the morning plan as preview markdown so the evening page can render it in the same
     * structure users expect from the saved log format.
     *
     * <p>Preconditions: the date may be null when a validation error occurs while posting the
     * evening form. In that case, the method returns the empty-state HTML.</p>
     *
     * @return HTML markup ready for the evening view.
     */
    public String buildMorningPlanHtml(LocalDate dateOrNull, UserAccountId userAccountId) {
        if (dateOrNull == null) {
            return EMPTY_MORNING_LOG_HTML;
        }

        String goals = mDaymarkService.readSection(dateOrNull, userAccountId, EDaymarkSectionType.GOALS);
        String focus = mDaymarkService.readSection(dateOrNull, userAccountId, EDaymarkSectionType.FOCUS);
        String challenges = mDaymarkService.readSection(dateOrNull, userAccountId, EDaymarkSectionType.CHALLENGES);

        String markdownText = buildMorningPreviewMarkdownText(goals, focus, challenges);
        if (markdownText.isBlank()) {
            return EMPTY_MORNING_LOG_HTML;
        }

        return mSimpleMarkdownRenderer.renderMarkdown(normalizePreviewMarkdownForRendering(markdownText));
    }

    public EveningFormDto buildEveningFormDto(LocalDate date, UserAccountId userAccountId) {
        String goals = mDaymarkService.readSection(date, userAccountId, EDaymarkSectionType.GOALS);
        Set<String> checkedGoalTexts = new HashSet<>(mDaymarkService.readCheckedGoalTexts(date, userAccountId));

        EveningFormDto eveningFormDto = new EveningFormDto();
        eveningFormDto.setDate(date);
        eveningFormDto.setGoals(buildEveningGoalItems(goals, checkedGoalTexts));
        eveningFormDto.setAchievements(mDaymarkService.readSection(date, userAccountId, EDaymarkSectionType.ACHIEVEMENTS));
        eveningFormDto.setImprovements(mDaymarkService.readSection(date, userAccountId, EDaymarkSectionType.IMPROVEMENTS));
        eveningFormDto.setGratitude(mDaymarkService.readSection(date, userAccountId, EDaymarkSectionType.GRATITUDE));
        eveningFormDto.setNotes(mDaymarkService.readSection(date, userAccountId, EDaymarkSectionType.NOTES));
        return eveningFormDto;
    }

    public Map<LocalDate, String> buildExportItemHtmlByDate(List<DaymarkLibraryItemDto> libraryItems) {
        Map<LocalDate, String> exportItemHtmlByDate = new LinkedHashMap<>();
        for (DaymarkLibraryItemDto libraryItem : libraryItems) {
            exportItemHtmlByDate.put(
                libraryItem.getDate(),
                buildPreviewHtml(libraryItem.getMarkdownText())
            );
        }

        return exportItemHtmlByDate;
    }

    public String buildPreviewHtml(String markdownText) {
        return mSimpleMarkdownRenderer.renderMarkdown(normalizePreviewMarkdownForRendering(markdownText));
    }

    private static List<EveningGoalItemDto> buildEveningGoalItems(String goals, Set<String> checkedGoalTexts) {
        List<EveningGoalItemDto> goalItems = new ArrayList<>();

        for (String goal : splitNonBlankLines(goals)) {
            EveningGoalItemDto eveningGoalItemDto = new EveningGoalItemDto();
            eveningGoalItemDto.setText(goal);
            eveningGoalItemDto.setDone(checkedGoalTexts.contains(goal));
            goalItems.add(eveningGoalItemDto);
        }

        return goalItems;
    }

    private static String buildMorningPreviewMarkdownText(String goals, String focus, String challenges) {
        StringBuilder markdownBuilder = new StringBuilder();
        appendPreviewSection(markdownBuilder, EDaymarkSectionType.GOALS, goals);
        appendPreviewSection(markdownBuilder, EDaymarkSectionType.FOCUS, focus);
        appendPreviewSection(markdownBuilder, EDaymarkSectionType.CHALLENGES, challenges);
        return markdownBuilder.toString().stripTrailing();
    }

    private static void appendPreviewSection(
        StringBuilder markdownBuilder,
        EDaymarkSectionType daymarkSectionType,
        String sectionBody
    ) {
        List<String> lines = splitNonBlankLines(sectionBody);
        if (lines.isEmpty()) {
            return;
        }

        if (markdownBuilder.length() > 0) {
            markdownBuilder.append("\r\n");
        }

        markdownBuilder.append(daymarkSectionType.getHeaderText()).append("\r\n\r\n");
        for (String line : lines) {
            markdownBuilder.append("- ").append(line).append("\r\n");
        }
    }

    private static String normalizePreviewMarkdownForRendering(String markdownTextOrNull) {
        if (markdownTextOrNull == null || markdownTextOrNull.isBlank()) {
            return "";
        }

        StringBuilder processedMarkdownBuilder = new StringBuilder();

        for (String line : markdownTextOrNull.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1)) {
            if (line.startsWith("#")) {
                int hashCount = 0;

                while (hashCount < line.length() && line.charAt(hashCount) == '#') {
                    hashCount++;
                }

                int newHashCount = Math.min(hashCount + 2, 6);
                String rest = line.substring(hashCount).trim();
                processedMarkdownBuilder.append("\r\n")
                    .append("#".repeat(newHashCount))
                    .append(" ")
                    .append(rest)
                    .append("\r\n");
                continue;
            }

            processedMarkdownBuilder.append(line).append("\r\n");
        }

        return processedMarkdownBuilder.toString().stripTrailing();
    }

    private static List<String> splitNonBlankLines(String textOrNull) {
        if (textOrNull == null || textOrNull.isBlank()) {
            return List.of();
        }

        return textOrNull.lines()
            .map(String::trim)
            .filter(line -> !line.isEmpty())
            .toList();
    }
}
