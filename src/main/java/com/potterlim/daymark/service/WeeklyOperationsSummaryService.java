package com.potterlim.daymark.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.potterlim.daymark.entity.DaymarkEntry;
import com.potterlim.daymark.entity.EOperationEventType;
import com.potterlim.daymark.entity.EUserRole;
import com.potterlim.daymark.repository.IDaymarkEntryRepository;
import com.potterlim.daymark.repository.IOperationUsageEventRepository;
import com.potterlim.daymark.repository.IUserAccountRepository;
import com.potterlim.daymark.support.DaymarkGoalCompletionCounts;
import com.potterlim.daymark.support.DaymarkGoalMarkdown;
import com.potterlim.daymark.support.DaymarkWeekRange;
import com.potterlim.daymark.support.EDaymarkSectionType;
import com.potterlim.daymark.support.WeeklyOperationMetricAverage;
import com.potterlim.daymark.support.WeeklyOperationMetricCount;
import com.potterlim.daymark.support.WeeklyOperationMetricPercent;
import com.potterlim.daymark.support.WeeklyOperationsSummary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WeeklyOperationsSummaryService {

    private static final EUserRole EXCLUDED_OPERATION_USER_ROLE = EUserRole.ADMIN;

    private final IUserAccountRepository mUserAccountRepository;
    private final IDaymarkEntryRepository mDaymarkEntryRepository;
    private final IOperationUsageEventRepository mOperationUsageEventRepository;

    public WeeklyOperationsSummaryService(
        IUserAccountRepository userAccountRepository,
        IDaymarkEntryRepository daymarkEntryRepository,
        IOperationUsageEventRepository operationUsageEventRepository
    ) {
        mUserAccountRepository = userAccountRepository;
        mDaymarkEntryRepository = daymarkEntryRepository;
        mOperationUsageEventRepository = operationUsageEventRepository;
    }

    @Transactional(readOnly = true)
    public WeeklyOperationsSummary buildWeeklySummary(DaymarkWeekRange weekRange) {
        if (weekRange == null) {
            throw new IllegalArgumentException("weekRange must not be null.");
        }

        LocalDate weekStartDate = weekRange.getStartDate();
        LocalDate weekEndDate = weekRange.getEndDate();
        LocalDateTime weekStartDateTime = weekStartDate.atStartOfDay();
        LocalDateTime weekEndExclusiveDateTime = weekEndDate.plusDays(1L).atStartOfDay();
        List<DaymarkEntry> weeklyEntries = mDaymarkEntryRepository.findEntriesWithinDateRangeExcludingUserRole(
            weekStartDate,
            weekEndDate,
            EXCLUDED_OPERATION_USER_ROLE
        );
        Set<Long> weeklyWritingUserIds = new HashSet<>();
        Set<Long> newWorkspaceActivatedUserIds = new HashSet<>();
        long weeklyWritingDays = 0L;
        long weeklyMorningEntries = 0L;
        long weeklyEveningEntries = 0L;
        long weeklyPlanReviewCompletedDays = 0L;
        DaymarkGoalCompletionCounts trackedGoalCompletionCounts = DaymarkGoalCompletionCounts.empty();

        for (DaymarkEntry daymarkEntry : weeklyEntries) {
            if (!daymarkEntry.hasAnyEntryContent()) {
                continue;
            }

            weeklyWritingUserIds.add(daymarkEntry.getUserAccountId().getValue());
            weeklyWritingDays += 1L;

            boolean hasMorningEntry = daymarkEntry.hasMorningEntry();
            boolean hasEveningEntry = daymarkEntry.hasEveningEntry();
            if (hasMorningEntry) {
                weeklyMorningEntries += 1L;
            }

            if (hasEveningEntry) {
                weeklyEveningEntries += 1L;
            }

            if (hasMorningEntry && hasEveningEntry) {
                weeklyPlanReviewCompletedDays += 1L;
            }

            if (hasMorningEntry && isNewWorkspaceWithinRange(
                daymarkEntry.getUserAccount().getCreatedAt(),
                weekStartDateTime,
                weekEndExclusiveDateTime
            )) {
                newWorkspaceActivatedUserIds.add(daymarkEntry.getUserAccountId().getValue());
            }

            DaymarkGoalCompletionCounts goalCompletionCounts =
                DaymarkGoalMarkdown.countGoalCompletion(daymarkEntry.readSection(EDaymarkSectionType.EVENING_GOALS));
            trackedGoalCompletionCounts = trackedGoalCompletionCounts.plus(goalCompletionCounts);
        }

        Set<Long> weeklyActiveUserIds =
            new HashSet<>(mOperationUsageEventRepository.findDistinctUserAccountIdsWithinExcludingUserRole(
                weekStartDateTime,
                weekEndExclusiveDateTime,
                EXCLUDED_OPERATION_USER_ROLE
            ));
        weeklyActiveUserIds.addAll(weeklyWritingUserIds);

        long weeklyActiveUsers = weeklyActiveUserIds.size();
        long weeklyWritingUsers = weeklyWritingUserIds.size();
        long totalRegisteredUsers = mUserAccountRepository.countExcludingUserRole(EXCLUDED_OPERATION_USER_ROLE);
        long newlyRegisteredUsers = mUserAccountRepository.countCreatedWithinExcludingUserRole(
            weekStartDateTime,
            weekEndExclusiveDateTime,
            EXCLUDED_OPERATION_USER_ROLE
        );
        long newWorkspaceActivatedUsers = newWorkspaceActivatedUserIds.size();

        WeeklyOperationMetricCount totalRegisteredUserCount = WeeklyOperationMetricCount.of(totalRegisteredUsers);
        WeeklyOperationMetricCount weeklyActiveUserCount = WeeklyOperationMetricCount.of(weeklyActiveUsers);
        WeeklyOperationMetricCount weeklyWritingUserCount = WeeklyOperationMetricCount.of(weeklyWritingUsers);
        WeeklyOperationMetricCount weeklyWritingDayCount = WeeklyOperationMetricCount.of(weeklyWritingDays);
        WeeklyOperationMetricCount weeklyMorningEntryCount = WeeklyOperationMetricCount.of(weeklyMorningEntries);
        WeeklyOperationMetricCount weeklyEveningEntryCount = WeeklyOperationMetricCount.of(weeklyEveningEntries);
        WeeklyOperationMetricCount weeklyPlanReviewCompletedDayCount =
            WeeklyOperationMetricCount.of(weeklyPlanReviewCompletedDays);
        WeeklyOperationMetricCount newlyRegisteredUserCount = WeeklyOperationMetricCount.of(newlyRegisteredUsers);
        WeeklyOperationMetricCount newWorkspaceActivatedUserCount =
            WeeklyOperationMetricCount.of(newWorkspaceActivatedUsers);
        WeeklyOperationMetricCount weeklyEntryCompletionCount = WeeklyOperationMetricCount.of(
            Math.addExact(weeklyMorningEntryCount.getValue(), weeklyEveningEntryCount.getValue())
        );
        WeeklyOperationMetricAverage averageWritingDaysPerActiveUser =
            WeeklyOperationMetricAverage.calculate(weeklyWritingDayCount, weeklyActiveUserCount);
        WeeklyOperationMetricAverage averageEntryCompletionsPerActiveUser =
            WeeklyOperationMetricAverage.calculate(weeklyEntryCompletionCount, weeklyActiveUserCount);
        WeeklyOperationMetricPercent planReviewConversionRatePercent =
            WeeklyOperationMetricPercent.calculate(weeklyPlanReviewCompletedDayCount, weeklyMorningEntryCount);
        WeeklyOperationMetricPercent newWorkspaceActivationRatePercent =
            WeeklyOperationMetricPercent.calculate(newWorkspaceActivatedUserCount, newlyRegisteredUserCount);

        return WeeklyOperationsSummary.createBuilder(weekRange)
            .setTotalRegisteredUsers(totalRegisteredUserCount)
            .setNewlyRegisteredUsers(newlyRegisteredUserCount)
            .setWeeklyActiveUsers(weeklyActiveUserCount)
            .setWeeklyWritingUsers(weeklyWritingUserCount)
            .setWeeklyWritingDays(weeklyWritingDayCount)
            .setWeeklyMorningEntries(weeklyMorningEntryCount)
            .setWeeklyEveningEntries(weeklyEveningEntryCount)
            .setWeeklyPlanReviewCompletedDays(weeklyPlanReviewCompletedDayCount)
            .setSignInSucceededCount(countMetricEvent(
                EOperationEventType.SIGN_IN_SUCCEEDED,
                weekStartDateTime,
                weekEndExclusiveDateTime
            ))
            .setSignInFailedCount(countMetricEvent(
                EOperationEventType.SIGN_IN_FAILED,
                weekStartDateTime,
                weekEndExclusiveDateTime
            ))
            .setWeeklyReviewViewedCount(countMetricEvent(
                EOperationEventType.WEEKLY_REVIEW_VIEWED,
                weekStartDateTime,
                weekEndExclusiveDateTime
            ))
            .setRecordLibraryViewedCount(countMetricEvent(
                EOperationEventType.RECORD_LIBRARY_VIEWED,
                weekStartDateTime,
                weekEndExclusiveDateTime
            ))
            .setMarkdownExportedCount(countMetricEvent(
                EOperationEventType.MARKDOWN_EXPORTED,
                weekStartDateTime,
                weekEndExclusiveDateTime
            ))
            .setPdfExportViewedCount(countMetricEvent(
                EOperationEventType.PDF_EXPORT_VIEWED,
                weekStartDateTime,
                weekEndExclusiveDateTime
            ))
            .setExportingUsers(countExportingUsersMetric(weekStartDateTime, weekEndExclusiveDateTime))
            .setNewWorkspaceActivatedUsers(newWorkspaceActivatedUserCount)
            .setAverageWritingDaysPerActiveUser(averageWritingDaysPerActiveUser)
            .setAverageEntryCompletionsPerActiveUser(averageEntryCompletionsPerActiveUser)
            .setPlanReviewConversionRatePercent(planReviewConversionRatePercent)
            .setNewWorkspaceActivationRatePercent(newWorkspaceActivationRatePercent)
            .setGoalCompletionRatePercent(WeeklyOperationMetricPercent.of(
                trackedGoalCompletionCounts.calculateCompletionRatePercent()
            ))
            .build();
    }

    private WeeklyOperationMetricCount countMetricEvent(
        EOperationEventType eventType,
        LocalDateTime weekStartDateTime,
        LocalDateTime weekEndExclusiveDateTime
    ) {
        long eventCount = mOperationUsageEventRepository.countByEventTypeWithinExcludingUserRole(
            eventType,
            weekStartDateTime,
            weekEndExclusiveDateTime,
            EXCLUDED_OPERATION_USER_ROLE
        );

        return WeeklyOperationMetricCount.of(eventCount);
    }

    private WeeklyOperationMetricCount countExportingUsersMetric(
        LocalDateTime weekStartDateTime,
        LocalDateTime weekEndExclusiveDateTime
    ) {
        long exportingUserCount =
            mOperationUsageEventRepository.countDistinctUserAccountIdsByEventTypesWithinExcludingUserRole(
                EnumSet.of(EOperationEventType.MARKDOWN_EXPORTED, EOperationEventType.PDF_EXPORT_VIEWED),
                weekStartDateTime,
                weekEndExclusiveDateTime,
                EXCLUDED_OPERATION_USER_ROLE
            );

        return WeeklyOperationMetricCount.of(exportingUserCount);
    }

    private static boolean isNewWorkspaceWithinRange(
        LocalDateTime createdAtOrNull,
        LocalDateTime startDateTime,
        LocalDateTime endExclusiveDateTime
    ) {
        return createdAtOrNull != null
            && !createdAtOrNull.isBefore(startDateTime)
            && createdAtOrNull.isBefore(endExclusiveDateTime);
    }
}
