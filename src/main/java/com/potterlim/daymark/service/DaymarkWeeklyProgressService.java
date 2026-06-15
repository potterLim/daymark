package com.potterlim.daymark.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.potterlim.daymark.dto.daymark.DaymarkDayStatusDto;
import com.potterlim.daymark.dto.daymark.WeeklyProgressItemDto;
import com.potterlim.daymark.dto.daymark.WeeklyProgressViewDto;
import com.potterlim.daymark.entity.UserAccountId;
import com.potterlim.daymark.support.DaymarkEntryDate;
import com.potterlim.daymark.support.DaymarkGoalCompletionCounts;
import com.potterlim.daymark.support.DaymarkTextLines;
import com.potterlim.daymark.support.DaymarkWeekOffset;
import com.potterlim.daymark.support.DaymarkWeekRange;
import com.potterlim.daymark.support.EDaymarkSectionType;
import org.springframework.stereotype.Service;

@Service
public class DaymarkWeeklyProgressService {

    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy. MM. dd.");

    private final IDaymarkService mDaymarkService;
    private final Clock mClock;

    public DaymarkWeeklyProgressService(IDaymarkService daymarkService, Clock clock) {
        mDaymarkService = daymarkService;
        mClock = clock;
    }

    public WeeklyProgressViewDto buildWeeklyProgressView(DaymarkWeekOffset weekOffset, UserAccountId userAccountId) {
        if (weekOffset == null) {
            throw new IllegalArgumentException("weekOffset must not be null.");
        }

        if (userAccountId == null) {
            throw new IllegalArgumentException("userAccountId must not be null.");
        }

        LocalDate currentDate = LocalDate.now(mClock);
        LocalDate referenceDate = weekOffset.calculateReferenceDateFrom(currentDate);
        DaymarkWeekRange selectedWeekRange = DaymarkWeekRange.containing(referenceDate);
        DaymarkWeekRange currentWeekRange = DaymarkWeekRange.containing(currentDate);
        WeeklyProgressAccumulator weeklyProgressAccumulator = buildWeeklyProgressAccumulator(
            referenceDate,
            userAccountId
        );
        DaymarkGoalCompletionCounts weeklyGoalCompletionCounts =
            weeklyProgressAccumulator.getWeeklyGoalCompletionCounts();

        return WeeklyProgressViewDto.createBuilder(weekOffset)
            .setWeeklyProgressItems(weeklyProgressAccumulator.getWeeklyProgressItems())
            .setWeeklyGoalCompletionCounts(weeklyGoalCompletionCounts)
            .setRangeLabel(buildWeekRangeLabel(selectedWeekRange))
            .setCurrentWeekRangeLabel(buildWeekRangeLabel(currentWeekRange))
            .setPreviousWeekRangeLabel(buildWeekRangeLabel(
                DaymarkWeekRange.containing(selectedWeekRange.getStartDate().minusDays(7L))
            ))
            .setNextWeekRangeLabel(buildWeekRangeLabel(
                DaymarkWeekRange.containing(selectedWeekRange.getStartDate().plusDays(7L))
            ))
            .setDefaultDate(currentDate)
            .build();
    }

    private WeeklyProgressAccumulator buildWeeklyProgressAccumulator(
        LocalDate referenceDate,
        UserAccountId userAccountId
    ) {
        WeeklyProgressAccumulator weeklyProgressAccumulator = new WeeklyProgressAccumulator();

        for (DaymarkDayStatusDto daymarkDayStatusDto : mDaymarkService.listWeek(
            DaymarkEntryDate.of(referenceDate),
            userAccountId
        )) {
            DaymarkEntryDate entryDate = DaymarkEntryDate.of(daymarkDayStatusDto.getDate());
            List<String> goalLines = DaymarkTextLines.splitNonBlankTrimmedLines(
                mDaymarkService.readSection(entryDate, userAccountId, EDaymarkSectionType.GOALS)
            );
            int achievedGoalCount = countAchievedGoals(daymarkDayStatusDto, goalLines, userAccountId);
            DaymarkGoalCompletionCounts goalCompletionCounts =
                DaymarkGoalCompletionCounts.of(achievedGoalCount, goalLines.size());

            weeklyProgressAccumulator.addProgressItem(
                WeeklyProgressItemDto.fromGoalCompletionCounts(
                    daymarkDayStatusDto.getDate(),
                    goalCompletionCounts
                )
            );
        }

        return weeklyProgressAccumulator;
    }

    private int countAchievedGoals(
        DaymarkDayStatusDto daymarkDayStatusDto,
        List<String> goalLines,
        UserAccountId userAccountId
    ) {
        if (!daymarkDayStatusDto.hasEveningEntry()) {
            return 0;
        }

        Set<String> checkedGoalTexts = new HashSet<>(
            mDaymarkService.readCheckedGoalTexts(DaymarkEntryDate.of(daymarkDayStatusDto.getDate()), userAccountId)
        );
        int achievedGoalCount = 0;
        for (String goalLine : goalLines) {
            if (checkedGoalTexts.contains(goalLine)) {
                achievedGoalCount++;
            }
        }

        return achievedGoalCount;
    }

    private static String buildWeekRangeLabel(DaymarkWeekRange weekRange) {
        return formatDisplayDate(weekRange.getStartDate()) + " ~ " + formatDisplayDate(weekRange.getEndDate());
    }

    private static String formatDisplayDate(LocalDate date) {
        return DISPLAY_DATE_FORMATTER.format(date);
    }

    private static final class WeeklyProgressAccumulator {

        private final List<WeeklyProgressItemDto> mWeeklyProgressItems = new ArrayList<>();
        private DaymarkGoalCompletionCounts mWeeklyGoalCompletionCounts = DaymarkGoalCompletionCounts.empty();

        private List<WeeklyProgressItemDto> getWeeklyProgressItems() {
            return mWeeklyProgressItems;
        }

        private DaymarkGoalCompletionCounts getWeeklyGoalCompletionCounts() {
            return mWeeklyGoalCompletionCounts;
        }

        private void addProgressItem(WeeklyProgressItemDto weeklyProgressItemDto) {
            mWeeklyProgressItems.add(weeklyProgressItemDto);
            mWeeklyGoalCompletionCounts = mWeeklyGoalCompletionCounts.plus(DaymarkGoalCompletionCounts.of(
                weeklyProgressItemDto.getAchievedGoalCount(),
                weeklyProgressItemDto.getTotalGoalCount()
            ));
        }
    }
}
