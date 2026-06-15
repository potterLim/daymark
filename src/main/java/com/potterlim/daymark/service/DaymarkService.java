package com.potterlim.daymark.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.potterlim.daymark.dto.daymark.DaymarkDayStatusDto;
import com.potterlim.daymark.dto.daymark.EveningReviewSaveCommand;
import com.potterlim.daymark.dto.daymark.MorningPlanSaveCommand;
import com.potterlim.daymark.entity.DaymarkEntry;
import com.potterlim.daymark.entity.UserAccount;
import com.potterlim.daymark.entity.UserAccountId;
import com.potterlim.daymark.repository.IDaymarkEntryRepository;
import com.potterlim.daymark.repository.IUserAccountRepository;
import com.potterlim.daymark.support.DaymarkEntryDate;
import com.potterlim.daymark.support.DaymarkGoalMarkdown;
import com.potterlim.daymark.support.DaymarkSectionText;
import com.potterlim.daymark.support.DaymarkTextLines;
import com.potterlim.daymark.support.DaymarkWeekRange;
import com.potterlim.daymark.support.EDaymarkSectionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DaymarkService implements IDaymarkService {

    private final IDaymarkEntryRepository mDaymarkEntryRepository;
    private final IUserAccountRepository mUserAccountRepository;

    public DaymarkService(
        IDaymarkEntryRepository daymarkEntryRepository,
        IUserAccountRepository userAccountRepository
    ) {
        mDaymarkEntryRepository = daymarkEntryRepository;
        mUserAccountRepository = userAccountRepository;
    }

    @Override
    @Transactional
    public void saveMorningPlan(MorningPlanSaveCommand morningPlanSaveCommand) {
        if (morningPlanSaveCommand == null) {
            throw new IllegalArgumentException("morningPlanSaveCommand must not be null.");
        }

        String goalsMarkdownList = DaymarkGoalMarkdown.buildGoalList(morningPlanSaveCommand.getGoals().getValue());
        saveSections(
            morningPlanSaveCommand.getEntryDate(),
            morningPlanSaveCommand.getUserAccountId(),
            List.of(
                DaymarkSectionUpdate.of(EDaymarkSectionType.GOALS, DaymarkSectionText.create(goalsMarkdownList)),
                DaymarkSectionUpdate.of(EDaymarkSectionType.FOCUS, morningPlanSaveCommand.getFocus()),
                DaymarkSectionUpdate.of(EDaymarkSectionType.CHALLENGES, morningPlanSaveCommand.getChallenges())
            )
        );
    }

    @Override
    @Transactional
    public void saveEveningReview(EveningReviewSaveCommand eveningReviewSaveCommand) {
        if (eveningReviewSaveCommand == null) {
            throw new IllegalArgumentException("eveningReviewSaveCommand must not be null.");
        }

        String checkedGoalMarkdownList = DaymarkGoalMarkdown.buildCheckedGoalList(eveningReviewSaveCommand.getGoals());
        saveSections(
            eveningReviewSaveCommand.getEntryDate(),
            eveningReviewSaveCommand.getUserAccountId(),
            List.of(
                DaymarkSectionUpdate.of(
                    EDaymarkSectionType.EVENING_GOALS,
                    DaymarkSectionText.create(checkedGoalMarkdownList)
                ),
                DaymarkSectionUpdate.of(EDaymarkSectionType.ACHIEVEMENTS, eveningReviewSaveCommand.getAchievements()),
                DaymarkSectionUpdate.of(EDaymarkSectionType.IMPROVEMENTS, eveningReviewSaveCommand.getImprovements()),
                DaymarkSectionUpdate.of(EDaymarkSectionType.GRATITUDE, eveningReviewSaveCommand.getGratitude()),
                DaymarkSectionUpdate.of(EDaymarkSectionType.NOTES, eveningReviewSaveCommand.getNotes())
            )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public String readSection(
        DaymarkEntryDate entryDate,
        UserAccountId userAccountId,
        EDaymarkSectionType daymarkSectionType
    ) {
        validateEntryQuery(entryDate, userAccountId);
        if (daymarkSectionType == null) {
            throw new IllegalArgumentException("daymarkSectionType must not be null.");
        }

        String sectionBody = findDaymarkEntry(entryDate, userAccountId)
            .map(daymarkEntry -> daymarkEntry.readSection(daymarkSectionType))
            .orElse("");

        if (sectionBody.isBlank()) {
            return "";
        }

        List<String> cleanedLines = new ArrayList<>();
        for (String line : DaymarkTextLines.splitLines(sectionBody)) {
            if (line.isBlank()) {
                continue;
            }

            if (line.startsWith("- ")) {
                cleanedLines.add(line.substring(2).stripTrailing());
            } else {
                cleanedLines.add(line.stripTrailing());
            }
        }

        return String.join("\r\n", cleanedLines);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DaymarkDayStatusDto> listWeek(DaymarkEntryDate referenceEntryDate, UserAccountId userAccountId) {
        validateEntryQuery(referenceEntryDate, userAccountId);

        DaymarkWeekRange weekRange = referenceEntryDate.containingWeekRange();
        List<DaymarkDayStatusDto> dayStatuses = new ArrayList<>();

        for (DaymarkEntry daymarkEntry : mDaymarkEntryRepository.findWeekEntries(
            userAccountId.getValue(),
            weekRange.getStartDate(),
            weekRange.getEndDate()
        )) {
            if (!daymarkEntry.hasAnyEntryContent()) {
                continue;
            }

            dayStatuses.add(createDayStatus(daymarkEntry));
        }

        return dayStatuses;
    }

    @Override
    @Transactional(readOnly = true)
    public String readEntryMarkdownContent(DaymarkEntryDate entryDate, UserAccountId userAccountId) {
        validateEntryQuery(entryDate, userAccountId);

        return findDaymarkEntry(entryDate, userAccountId)
            .map(DaymarkEntryMarkdownFormatter::format)
            .orElse("");
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> readCheckedGoalTexts(DaymarkEntryDate entryDate, UserAccountId userAccountId) {
        validateEntryQuery(entryDate, userAccountId);

        return findDaymarkEntry(entryDate, userAccountId)
            .map(DaymarkEntry::readCheckedGoalTexts)
            .orElse(List.of());
    }

    private static void validateEntryQuery(DaymarkEntryDate entryDate, UserAccountId userAccountId) {
        if (entryDate == null) {
            throw new IllegalArgumentException("entryDate must not be null.");
        }

        if (userAccountId == null) {
            throw new IllegalArgumentException("userAccountId must not be null.");
        }
    }

    private void saveSections(
        DaymarkEntryDate entryDate,
        UserAccountId userAccountId,
        List<DaymarkSectionUpdate> sectionUpdates
    ) {
        List<DaymarkSectionUpdate> normalizedSectionUpdates = normalizeSectionUpdates(sectionUpdates);
        Optional<DaymarkEntry> daymarkEntryOptional = findDaymarkEntry(entryDate, userAccountId);
        if (daymarkEntryOptional.isEmpty() && hasNoSectionBody(normalizedSectionUpdates)) {
            return;
        }

        DaymarkEntry daymarkEntry = daymarkEntryOptional
            .orElseGet(() -> createDaymarkEntry(entryDate, userAccountId));

        for (DaymarkSectionUpdate sectionUpdate : normalizedSectionUpdates) {
            daymarkEntry.writeSection(sectionUpdate.getDaymarkSectionType(), sectionUpdate.getBody());
        }

        saveOrDelete(daymarkEntry);
    }

    private static List<DaymarkSectionUpdate> normalizeSectionUpdates(List<DaymarkSectionUpdate> sectionUpdates) {
        List<DaymarkSectionUpdate> normalizedSectionUpdates = new ArrayList<>();
        for (DaymarkSectionUpdate sectionUpdate : sectionUpdates) {
            normalizedSectionUpdates.add(DaymarkSectionUpdate.of(
                sectionUpdate.getDaymarkSectionType(),
                sectionUpdate.getBody().normalizeSectionBody()
            ));
        }

        return normalizedSectionUpdates;
    }

    private static boolean hasNoSectionBody(List<DaymarkSectionUpdate> sectionUpdates) {
        for (DaymarkSectionUpdate sectionUpdate : sectionUpdates) {
            if (!sectionUpdate.getBody().isBlank()) {
                return false;
            }
        }

        return true;
    }

    private void saveOrDelete(DaymarkEntry daymarkEntry) {
        if (!daymarkEntry.hasAnyEntryContent()) {
            mDaymarkEntryRepository.delete(daymarkEntry);
            return;
        }

        mDaymarkEntryRepository.save(daymarkEntry);
    }

    private static DaymarkDayStatusDto createDayStatus(DaymarkEntry daymarkEntry) {
        boolean hasMorningEntry = daymarkEntry.hasMorningEntry();
        boolean hasEveningEntry = daymarkEntry.hasEveningEntry();
        if (hasMorningEntry && hasEveningEntry) {
            return DaymarkDayStatusDto.createMorningAndEvening(daymarkEntry.getEntryDate());
        }

        if (hasMorningEntry) {
            return DaymarkDayStatusDto.createMorningOnly(daymarkEntry.getEntryDate());
        }

        return DaymarkDayStatusDto.createEveningOnly(daymarkEntry.getEntryDate());
    }

    private Optional<DaymarkEntry> findDaymarkEntry(DaymarkEntryDate entryDate, UserAccountId userAccountId) {
        return mDaymarkEntryRepository.findByUserAccountIdAndLogDate(
            userAccountId.getValue(),
            entryDate.getValue()
        );
    }

    private DaymarkEntry createDaymarkEntry(DaymarkEntryDate entryDate, UserAccountId userAccountId) {
        UserAccount userAccount = mUserAccountRepository.getReferenceById(userAccountId.getValue());
        return DaymarkEntry.create(userAccount, entryDate);
    }

    private static final class DaymarkSectionUpdate {

        private final EDaymarkSectionType mDaymarkSectionType;
        private final DaymarkSectionText mBody;

        private DaymarkSectionUpdate(EDaymarkSectionType daymarkSectionType, DaymarkSectionText body) {
            if (daymarkSectionType == null) {
                throw new IllegalArgumentException("daymarkSectionType must not be null.");
            }

            if (body == null) {
                throw new IllegalArgumentException("body must not be null.");
            }

            mDaymarkSectionType = daymarkSectionType;
            mBody = body;
        }

        private static DaymarkSectionUpdate of(EDaymarkSectionType daymarkSectionType, DaymarkSectionText body) {
            return new DaymarkSectionUpdate(daymarkSectionType, body);
        }

        private EDaymarkSectionType getDaymarkSectionType() {
            return mDaymarkSectionType;
        }

        private DaymarkSectionText getBody() {
            return mBody;
        }
    }
}
