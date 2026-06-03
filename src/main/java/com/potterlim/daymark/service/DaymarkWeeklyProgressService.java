package com.potterlim.daymark.service;

import java.time.Clock;
import java.time.DayOfWeek;
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

    public WeeklyProgressViewDto buildWeeklyProgressView(int weekOffset, UserAccountId userAccountId) {
        LocalDate currentDate = LocalDate.now(mClock);
        LocalDate referenceDate = currentDate.plusDays((long) weekOffset * 7L);
        LocalDate startDate = resolveWeekStartDate(referenceDate);
        WeeklyProgressAccumulator weeklyProgressAccumulator = buildWeeklyProgressAccumulator(referenceDate, userAccountId);

        return new WeeklyProgressViewDto(
            weeklyProgressAccumulator.getWeeklyProgressItems(),
            weeklyProgressAccumulator.getWeeklyAchievedGoalCount(),
            weeklyProgressAccumulator.getWeeklyTotalGoalCount(),
            calculateCompletionPercent(
                weeklyProgressAccumulator.getWeeklyAchievedGoalCount(),
                weeklyProgressAccumulator.getWeeklyTotalGoalCount()
            ),
            weekOffset,
            buildWeekRangeLabel(startDate),
            buildWeekRangeLabel(resolveWeekStartDate(currentDate)),
            buildWeekRangeLabel(startDate.minusDays(7L)),
            buildWeekRangeLabel(startDate.plusDays(7L)),
            currentDate
        );
    }

    private WeeklyProgressAccumulator buildWeeklyProgressAccumulator(
        LocalDate referenceDate,
        UserAccountId userAccountId
    ) {
        WeeklyProgressAccumulator weeklyProgressAccumulator = new WeeklyProgressAccumulator();

        for (DaymarkDayStatusDto daymarkDayStatusDto : mDaymarkService.listWeek(referenceDate, userAccountId)) {
            List<String> goals = splitNonBlankLines(
                mDaymarkService.readSection(daymarkDayStatusDto.getDate(), userAccountId, EDaymarkSectionType.GOALS)
            );
            int achievedGoalCount = countAchievedGoals(daymarkDayStatusDto, goals, userAccountId);
            int totalGoalCount = goals.size();

            weeklyProgressAccumulator.addProgressItem(
                new WeeklyProgressItemDto(
                    daymarkDayStatusDto.getDate(),
                    achievedGoalCount,
                    totalGoalCount,
                    calculateCompletionPercent(achievedGoalCount, totalGoalCount)
                )
            );
        }

        return weeklyProgressAccumulator;
    }

    private int countAchievedGoals(
        DaymarkDayStatusDto daymarkDayStatusDto,
        List<String> goals,
        UserAccountId userAccountId
    ) {
        if (!daymarkDayStatusDto.hasEveningEntry()) {
            return 0;
        }

        Set<String> checkedGoalTexts = new HashSet<>(mDaymarkService.readCheckedGoalTexts(daymarkDayStatusDto.getDate(), userAccountId));
        int achievedGoalCount = 0;
        for (String goal : goals) {
            if (checkedGoalTexts.contains(goal)) {
                achievedGoalCount++;
            }
        }

        return achievedGoalCount;
    }

    private static int calculateCompletionPercent(int achievedGoalCount, int totalGoalCount) {
        if (totalGoalCount == 0) {
            return 0;
        }

        return (int) ((achievedGoalCount / (double) totalGoalCount) * 100);
    }

    private static LocalDate resolveWeekStartDate(LocalDate referenceDate) {
        return referenceDate.minusDays(referenceDate.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue());
    }

    private static String buildWeekRangeLabel(LocalDate startDate) {
        return formatDisplayDate(startDate) + " ~ " + formatDisplayDate(startDate.plusDays(6L));
    }

    private static String formatDisplayDate(LocalDate date) {
        return DISPLAY_DATE_FORMATTER.format(date);
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

    private static final class WeeklyProgressAccumulator {

        private final List<WeeklyProgressItemDto> mWeeklyProgressItems = new ArrayList<>();
        private int mWeeklyAchievedGoalCount;
        private int mWeeklyTotalGoalCount;

        private List<WeeklyProgressItemDto> getWeeklyProgressItems() {
            return mWeeklyProgressItems;
        }

        private int getWeeklyAchievedGoalCount() {
            return mWeeklyAchievedGoalCount;
        }

        private int getWeeklyTotalGoalCount() {
            return mWeeklyTotalGoalCount;
        }

        private void addProgressItem(WeeklyProgressItemDto weeklyProgressItemDto) {
            mWeeklyProgressItems.add(weeklyProgressItemDto);
            mWeeklyAchievedGoalCount += weeklyProgressItemDto.getAchievedGoalCount();
            mWeeklyTotalGoalCount += weeklyProgressItemDto.getTotalGoalCount();
        }
    }
}
