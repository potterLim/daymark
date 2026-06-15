package com.potterlim.daymark.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.potterlim.daymark.dto.daymark.DaymarkLibraryItemDto;
import com.potterlim.daymark.dto.daymark.EveningFormDto;
import com.potterlim.daymark.dto.daymark.EveningGoalItemDto;
import com.potterlim.daymark.entity.UserAccountId;
import com.potterlim.daymark.support.DaymarkEntryDate;
import com.potterlim.daymark.support.DaymarkPreviewMarkdown;
import com.potterlim.daymark.support.DaymarkSectionText;
import com.potterlim.daymark.support.DaymarkTextLines;
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

        if (userAccountId == null) {
            throw new IllegalArgumentException("userAccountId must not be null.");
        }

        DaymarkEntryDate entryDate = DaymarkEntryDate.of(dateOrNull);
        DaymarkPreviewMarkdown previewMarkdown = DaymarkPreviewMarkdown.fromMorningSections(
            readSectionText(entryDate, userAccountId, EDaymarkSectionType.GOALS),
            readSectionText(entryDate, userAccountId, EDaymarkSectionType.FOCUS),
            readSectionText(entryDate, userAccountId, EDaymarkSectionType.CHALLENGES)
        );
        if (previewMarkdown.isBlank()) {
            return EMPTY_MORNING_LOG_HTML;
        }

        return buildPreviewHtml(previewMarkdown);
    }

    public EveningFormDto buildEveningFormDto(LocalDate date, UserAccountId userAccountId) {
        if (date == null) {
            throw new IllegalArgumentException("date must not be null.");
        }

        if (userAccountId == null) {
            throw new IllegalArgumentException("userAccountId must not be null.");
        }

        DaymarkEntryDate entryDate = DaymarkEntryDate.of(date);
        String goals = mDaymarkService.readSection(entryDate, userAccountId, EDaymarkSectionType.GOALS);
        Set<String> checkedGoalTexts = new HashSet<>(mDaymarkService.readCheckedGoalTexts(entryDate, userAccountId));

        EveningFormDto eveningFormDto = new EveningFormDto();
        eveningFormDto.setDate(date);
        eveningFormDto.setGoals(buildEveningGoalItems(goals, checkedGoalTexts));
        eveningFormDto.setAchievements(mDaymarkService.readSection(
            entryDate,
            userAccountId,
            EDaymarkSectionType.ACHIEVEMENTS
        ));
        eveningFormDto.setImprovements(mDaymarkService.readSection(
            entryDate,
            userAccountId,
            EDaymarkSectionType.IMPROVEMENTS
        ));
        eveningFormDto.setGratitude(mDaymarkService.readSection(
            entryDate,
            userAccountId,
            EDaymarkSectionType.GRATITUDE
        ));
        eveningFormDto.setNotes(mDaymarkService.readSection(entryDate, userAccountId, EDaymarkSectionType.NOTES));
        return eveningFormDto;
    }

    public Map<LocalDate, String> buildExportItemHtmlByDate(List<DaymarkLibraryItemDto> libraryItems) {
        if (libraryItems == null) {
            throw new IllegalArgumentException("libraryItems must not be null.");
        }

        Map<LocalDate, String> exportItemHtmlByDate = new LinkedHashMap<>();
        for (DaymarkLibraryItemDto libraryItem : libraryItems) {
            exportItemHtmlByDate.put(
                libraryItem.getDate(),
                buildPreviewHtml(DaymarkPreviewMarkdown.create(libraryItem.getMarkdownText()))
            );
        }

        return exportItemHtmlByDate;
    }

    public String buildPreviewHtml(DaymarkPreviewMarkdown previewMarkdown) {
        if (previewMarkdown == null) {
            throw new IllegalArgumentException("previewMarkdown must not be null.");
        }

        return mSimpleMarkdownRenderer.renderMarkdown(previewMarkdown.normalizeForRendering().getValue());
    }

    private DaymarkSectionText readSectionText(
        DaymarkEntryDate entryDate,
        UserAccountId userAccountId,
        EDaymarkSectionType daymarkSectionType
    ) {
        return DaymarkSectionText.create(mDaymarkService.readSection(entryDate, userAccountId, daymarkSectionType));
    }

    private static List<EveningGoalItemDto> buildEveningGoalItems(String goals, Set<String> checkedGoalTexts) {
        List<EveningGoalItemDto> goalItems = new ArrayList<>();

        for (String goal : DaymarkTextLines.splitNonBlankTrimmedLines(goals)) {
            EveningGoalItemDto eveningGoalItemDto = new EveningGoalItemDto();
            eveningGoalItemDto.setText(goal);
            eveningGoalItemDto.setDone(checkedGoalTexts.contains(goal));
            goalItems.add(eveningGoalItemDto);
        }

        return goalItems;
    }

}
