package com.potterlim.daymark.service;

import java.util.List;
import com.potterlim.daymark.dto.daymark.DaymarkDayStatusDto;
import com.potterlim.daymark.dto.daymark.EveningReviewSaveCommand;
import com.potterlim.daymark.dto.daymark.MorningPlanSaveCommand;
import com.potterlim.daymark.entity.UserAccountId;
import com.potterlim.daymark.support.DaymarkEntryDate;
import com.potterlim.daymark.support.EDaymarkSectionType;

public interface IDaymarkService {

    void saveMorningPlan(MorningPlanSaveCommand morningPlanSaveCommand);

    void saveEveningReview(EveningReviewSaveCommand eveningReviewSaveCommand);

    /**
     * Reads the content stored under a given Daymark section.
     *
     * <p>Preconditions: the date, user account id, and section type must already be validated by
     * the caller. When the entry or section does not exist, the method returns an empty string.</p>
     *
     * @return The section body with list prefixes removed for form editing.
     */
    String readSection(DaymarkEntryDate entryDate, UserAccountId userAccountId, EDaymarkSectionType daymarkSectionType);

    /**
     * Lists the available Daymark entries for the week that contains the reference date.
     *
     * <p>Preconditions: the reference date and user account id must already be validated by the
     * caller. Only dates with persisted entry content are returned.</p>
     *
     * @return Ordered day status entries for the matching week.
     */
    List<DaymarkDayStatusDto> listWeek(DaymarkEntryDate referenceEntryDate, UserAccountId userAccountId);

    /**
     * Reads the reconstructed markdown content for preview rendering and detailed analysis.
     *
     * <p>Preconditions: the date and user account id must already be validated by the caller. When
     * the target entry does not exist, the method returns an empty string.</p>
     *
     * @return The full markdown representation of the stored entry, or an empty string when the
     * entry is missing.
     */
    String readEntryMarkdownContent(DaymarkEntryDate entryDate, UserAccountId userAccountId);

    /**
     * Reads the goal texts that are marked as completed in the evening section.
     *
     * <p>Preconditions: the date and user account id must already be validated by the caller. When
     * the target entry does not exist, the method returns an empty list.</p>
     *
     * @return Checked goal texts in the order they appear in the saved evening checklist.
     */
    List<String> readCheckedGoalTexts(DaymarkEntryDate entryDate, UserAccountId userAccountId);
}
