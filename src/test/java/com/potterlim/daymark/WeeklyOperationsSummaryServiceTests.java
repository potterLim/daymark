package com.potterlim.daymark;

import java.time.LocalDate;
import java.util.List;
import com.potterlim.daymark.dto.auth.RegisterUserAccountCommand;
import com.potterlim.daymark.support.DaymarkGoalCheckItem;
import com.potterlim.daymark.dto.daymark.EveningReviewSaveCommand;
import com.potterlim.daymark.dto.daymark.MorningPlanSaveCommand;
import com.potterlim.daymark.entity.DaymarkEntry;
import com.potterlim.daymark.entity.EOperationEventType;
import com.potterlim.daymark.entity.UserAccount;
import com.potterlim.daymark.repository.IDaymarkEntryRepository;
import com.potterlim.daymark.repository.IOperationUsageEventRepository;
import com.potterlim.daymark.repository.IUserAccountRepository;
import com.potterlim.daymark.repository.IWeeklyOperationMetricSnapshotRepository;
import com.potterlim.daymark.service.IDaymarkService;
import com.potterlim.daymark.service.IUserAccountService;
import com.potterlim.daymark.service.OperationUsageEventService;
import com.potterlim.daymark.service.WeeklyOperationMetricSnapshotService;
import com.potterlim.daymark.support.WeeklyOperationsSummary;
import com.potterlim.daymark.service.WeeklyOperationsSummaryService;
import com.potterlim.daymark.support.DaymarkEntryDate;
import com.potterlim.daymark.support.DaymarkWeekRange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class WeeklyOperationsSummaryServiceTests {

    @Autowired
    private WeeklyOperationsSummaryService mWeeklyOperationsSummaryService;

    @Autowired
    private IUserAccountService mUserAccountService;

    @Autowired
    private IDaymarkService mDaymarkService;

    @Autowired
    private IDaymarkEntryRepository mDaymarkEntryRepository;

    @Autowired
    private OperationUsageEventService mOperationUsageEventService;

    @Autowired
    private WeeklyOperationMetricSnapshotService mWeeklyOperationMetricSnapshotService;

    @Autowired
    private IUserAccountRepository mUserAccountRepository;

    @Autowired
    private IOperationUsageEventRepository mOperationUsageEventRepository;

    @Autowired
    private IWeeklyOperationMetricSnapshotRepository mWeeklyOperationMetricSnapshotRepository;

    @BeforeEach
    void setUpTestEnvironment() {
        mWeeklyOperationMetricSnapshotRepository.deleteAll();
        mOperationUsageEventRepository.deleteAll();
        mDaymarkEntryRepository.deleteAll();
        mUserAccountRepository.deleteAll();
    }

    @Test
    void weeklyOperationsSummaryShouldAggregateWeeklyUsageMetrics() {
        LocalDate weekEndDate = LocalDate.now();
        LocalDate weekStartDate = weekEndDate.minusDays(6L);

        UserAccount firstUser = mUserAccountService.registerUserAccount(
            RegisterUserAccountCommand.createFromRawInput("summary-user-1", "summary-user-1@example.com", "pass1234")
        );
        UserAccount secondUser = mUserAccountService.registerUserAccount(
            RegisterUserAccountCommand.createFromRawInput("summary-user-2", "summary-user-2@example.com", "pass1234")
        );
        UserAccount thirdUser = mUserAccountService.registerUserAccount(
            RegisterUserAccountCommand.createFromRawInput("summary-user-3", "summary-user-3@example.com", "pass1234")
        );
        UserAccount adminUser = mUserAccountService.registerUserAccount(
            RegisterUserAccountCommand.createFromRawInput("summary-admin", "summary-admin@example.com", "pass1234")
        );
        adminUser.grantAdministratorRole();
        mUserAccountRepository.saveAndFlush(adminUser);

        saveMorningPlan(weekStartDate, firstUser, "목표 1\r\n목표 2");
        saveEveningReview(weekStartDate, firstUser, createCompletedGoal("목표 1"), createPendingGoal("목표 2"));
        saveMorningPlan(weekStartDate.plusDays(2L), firstUser, "리뷰 준비");
        saveEveningReview(weekStartDate.plusDays(4L), secondUser, createCompletedGoal("목표 3"));
        mDaymarkEntryRepository.save(DaymarkEntry.create(thirdUser, DaymarkEntryDate.of(weekStartDate.plusDays(5L))));
        saveMorningPlan(weekStartDate.plusDays(1L), adminUser, "관리자 점검");
        saveEveningReview(weekStartDate.plusDays(1L), adminUser, createCompletedGoal("관리자 점검"));
        recordUserEvent(EOperationEventType.SIGN_IN_SUCCEEDED, firstUser);
        recordUserEvent(EOperationEventType.RECORD_LIBRARY_VIEWED, firstUser);
        recordUserEvent(EOperationEventType.WEEKLY_REVIEW_VIEWED, firstUser);
        recordUserEvent(EOperationEventType.MARKDOWN_EXPORTED, firstUser);
        recordUserEvent(EOperationEventType.SIGN_IN_SUCCEEDED, adminUser);
        recordUserEvent(EOperationEventType.RECORD_LIBRARY_VIEWED, adminUser);
        recordUserEvent(EOperationEventType.WEEKLY_REVIEW_VIEWED, adminUser);
        recordUserEvent(EOperationEventType.MARKDOWN_EXPORTED, adminUser);
        mOperationUsageEventService.recordAnonymousEvent(EOperationEventType.SIGN_IN_FAILED);

        WeeklyOperationsSummary weeklyOperationsSummary =
            mWeeklyOperationsSummaryService.buildWeeklySummary(DaymarkWeekRange.of(weekStartDate, weekEndDate));

        assertEquals(3L, weeklyOperationsSummary.getTotalRegisteredUsers());
        assertEquals(3L, weeklyOperationsSummary.getNewlyRegisteredUsers());
        assertEquals(2L, weeklyOperationsSummary.getWeeklyActiveUsers());
        assertEquals(2L, weeklyOperationsSummary.getWeeklyWritingUsers());
        assertEquals(3L, weeklyOperationsSummary.getWeeklyWritingDays());
        assertEquals(2L, weeklyOperationsSummary.getWeeklyMorningEntries());
        assertEquals(2L, weeklyOperationsSummary.getWeeklyEveningEntries());
        assertEquals(1L, weeklyOperationsSummary.getWeeklyPlanReviewCompletedDays());
        assertEquals(1L, weeklyOperationsSummary.getSignInSucceededCount());
        assertEquals(1L, weeklyOperationsSummary.getSignInFailedCount());
        assertEquals(1L, weeklyOperationsSummary.getWeeklyReviewViewedCount());
        assertEquals(1L, weeklyOperationsSummary.getRecordLibraryViewedCount());
        assertEquals(1L, weeklyOperationsSummary.getMarkdownExportedCount());
        assertEquals(0L, weeklyOperationsSummary.getPdfExportViewedCount());
        assertEquals(1L, weeklyOperationsSummary.getExportingUsers());
        assertEquals(1L, weeklyOperationsSummary.getNewWorkspaceActivatedUsers());
        assertEquals(1.5, weeklyOperationsSummary.getAverageWritingDaysPerActiveUser(), 0.0001);
        assertEquals(2.0, weeklyOperationsSummary.getAverageEntryCompletionsPerActiveUser(), 0.0001);
        assertEquals(50.0, weeklyOperationsSummary.getPlanReviewConversionRatePercent(), 0.0001);
        assertEquals(
            33.333333333333336,
            weeklyOperationsSummary.getNewWorkspaceActivationRatePercent(),
            0.0001
        );
        assertEquals(66.66666666666667, weeklyOperationsSummary.getGoalCompletionRatePercent(), 0.0001);

        mWeeklyOperationMetricSnapshotService.saveWeeklySnapshot(weeklyOperationsSummary);

        assertEquals(1L, mWeeklyOperationMetricSnapshotRepository.count());
    }

    private void recordUserEvent(EOperationEventType operationEventType, UserAccount userAccount) {
        mOperationUsageEventService.recordUserEvent(operationEventType, userAccount.getUserAccountId());
    }

    private void saveMorningPlan(LocalDate date, UserAccount userAccount, String goals) {
        mDaymarkService.saveMorningPlan(MorningPlanSaveCommand.createFromRawInput(
            date,
            userAccount.getUserAccountId(),
            goals,
            "",
            ""
        ));
    }

    private void saveEveningReview(
        LocalDate date,
        UserAccount userAccount,
        DaymarkGoalCheckItem... goalItems
    ) {
        mDaymarkService.saveEveningReview(EveningReviewSaveCommand.createFromRawInput(
            date,
            userAccount.getUserAccountId(),
            List.of(goalItems),
            "",
            "",
            "",
            ""
        ));
    }

    private static DaymarkGoalCheckItem createCompletedGoal(String text) {
        return DaymarkGoalCheckItem.createCompleted(text);
    }

    private static DaymarkGoalCheckItem createPendingGoal(String text) {
        return DaymarkGoalCheckItem.createPending(text);
    }
}
