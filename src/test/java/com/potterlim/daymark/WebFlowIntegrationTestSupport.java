package com.potterlim.daymark;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import com.potterlim.daymark.dto.auth.GoogleRegistrationSession;
import com.potterlim.daymark.dto.daymark.EveningReviewSaveCommand;
import com.potterlim.daymark.dto.daymark.MorningPlanSaveCommand;
import com.potterlim.daymark.entity.UserAccount;
import com.potterlim.daymark.entity.WeeklyOperationMetricSnapshot;
import com.potterlim.daymark.repository.IDaymarkEntryRepository;
import com.potterlim.daymark.repository.IOperationUsageEventRepository;
import com.potterlim.daymark.repository.IUserAccountRepository;
import com.potterlim.daymark.repository.IWeeklyOperationMetricSnapshotRepository;
import com.potterlim.daymark.security.InMemoryRateLimiter;
import com.potterlim.daymark.service.AdministratorAccountInitializer;
import com.potterlim.daymark.service.IAlertNotificationService;
import com.potterlim.daymark.service.IDaymarkService;
import com.potterlim.daymark.service.IUserAccountService;
import com.potterlim.daymark.service.OperationUsageEventService;
import com.potterlim.daymark.support.DaymarkGoalCheckItem;
import com.potterlim.daymark.support.DaymarkWeekRange;
import com.potterlim.daymark.support.WeeklyOperationMetricAverage;
import com.potterlim.daymark.support.WeeklyOperationMetricCount;
import com.potterlim.daymark.support.WeeklyOperationMetricPercent;
import com.potterlim.daymark.support.WeeklyOperationsSummary;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class WebFlowIntegrationTestSupport {

    protected static final ZoneId TEST_ZONE_ID = ZoneId.of("Asia/Seoul");
    protected static final LocalDate TEST_CURRENT_DATE = LocalDate.of(2026, 4, 24);

    @Autowired
    protected MockMvc mMockMvc;

    @Autowired
    protected IDaymarkService mDaymarkService;

    @Autowired
    protected IUserAccountService mUserAccountService;

    @Autowired
    protected IDaymarkEntryRepository mDaymarkEntryRepository;

    @Autowired
    protected IUserAccountRepository mUserAccountRepository;

    @Autowired
    protected IOperationUsageEventRepository mOperationUsageEventRepository;

    @Autowired
    protected OperationUsageEventService mOperationUsageEventService;

    @Autowired
    protected AdministratorAccountInitializer mAdministratorAccountInitializer;

    @Autowired
    protected IWeeklyOperationMetricSnapshotRepository mWeeklyOperationMetricSnapshotRepository;

    @Autowired
    protected InMemoryRateLimiter mInMemoryRateLimiter;

    @MockitoBean
    protected IAlertNotificationService mAlertNotificationService;

    @MockitoBean
    protected Clock mClock;

    @BeforeEach
    void setUpTestEnvironment() {
        Instant testInstant = TEST_CURRENT_DATE.atStartOfDay(TEST_ZONE_ID).toInstant();
        when(mClock.instant()).thenReturn(testInstant);
        when(mClock.getZone()).thenReturn(TEST_ZONE_ID);

        mWeeklyOperationMetricSnapshotRepository.deleteAll();
        mOperationUsageEventRepository.deleteAll();
        mDaymarkEntryRepository.deleteAll();
        mUserAccountRepository.deleteAll();
        mInMemoryRateLimiter.clear();
    }

    protected static MockHttpSession createGoogleRegistrationSession(String googleSubject, String emailAddress) {
        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute(
            GoogleRegistrationSession.SESSION_ATTRIBUTE_NAME,
            new GoogleRegistrationSession(googleSubject, emailAddress, "")
        );
        return mockHttpSession;
    }

    protected void savePreviousWeeklyOperationsSnapshot() {
        LocalDate previousWeekStartDate = TEST_CURRENT_DATE.minusDays(11L);
        saveWeeklyOperationsSnapshot(previousWeekStartDate, 1L, 1L, 33.3);
    }

    protected void saveMorningPlan(LocalDate date, UserAccount userAccount, String goals) {
        saveMorningPlan(date, userAccount, goals, "", "");
    }

    protected void saveMorningPlan(
        LocalDate date,
        UserAccount userAccount,
        String goals,
        String focus,
        String challenges
    ) {
        mDaymarkService.saveMorningPlan(MorningPlanSaveCommand.createFromRawInput(
            date,
            userAccount.getUserAccountId(),
            goals,
            focus,
            challenges
        ));
    }

    protected void saveEveningReview(
        LocalDate date,
        UserAccount userAccount,
        DaymarkGoalCheckItem... goalItems
    ) {
        saveEveningReview(date, userAccount, List.of(goalItems), "", "", "", "");
    }

    protected void saveEveningReview(
        LocalDate date,
        UserAccount userAccount,
        List<DaymarkGoalCheckItem> goalItems,
        String achievements,
        String improvements,
        String gratitude,
        String notes
    ) {
        mDaymarkService.saveEveningReview(EveningReviewSaveCommand.createFromRawInput(
            date,
            userAccount.getUserAccountId(),
            goalItems,
            achievements,
            improvements,
            gratitude,
            notes
        ));
    }

    protected static DaymarkGoalCheckItem createCompletedGoal(String text) {
        return DaymarkGoalCheckItem.createCompleted(text);
    }

    protected static DaymarkGoalCheckItem createPendingGoal(String text) {
        return DaymarkGoalCheckItem.createPending(text);
    }

    protected void saveWeeklyOperationsSnapshot(
        LocalDate weekStartDate,
        long weeklyActiveUsers,
        long weeklyWritingUsers,
        double goalCompletionRatePercent
    ) {
        LocalDate weekEndDate = weekStartDate.plusDays(6L);
        WeeklyOperationsSummary previousWeeklyOperationsSummary = WeeklyOperationsSummary.createBuilder(
                DaymarkWeekRange.of(weekStartDate, weekEndDate)
            )
            .setTotalRegisteredUsers(createMetricCount(3L))
            .setNewlyRegisteredUsers(createMetricCount(1L))
            .setWeeklyActiveUsers(createMetricCount(weeklyActiveUsers))
            .setWeeklyWritingUsers(createMetricCount(weeklyWritingUsers))
            .setWeeklyWritingDays(createMetricCount(2L))
            .setWeeklyMorningEntries(createMetricCount(2L))
            .setWeeklyEveningEntries(createMetricCount(1L))
            .setWeeklyPlanReviewCompletedDays(createMetricCount(1L))
            .setSignInSucceededCount(createMetricCount(2L))
            .setSignInFailedCount(createMetricCount(1L))
            .setWeeklyReviewViewedCount(createMetricCount(1L))
            .setRecordLibraryViewedCount(createMetricCount(1L))
            .setMarkdownExportedCount(createMetricCount(1L))
            .setPdfExportViewedCount(createMetricCount(0L))
            .setExportingUsers(createMetricCount(1L))
            .setNewWorkspaceActivatedUsers(createMetricCount(1L))
            .setAverageWritingDaysPerActiveUser(createMetricAverage(2.0))
            .setAverageEntryCompletionsPerActiveUser(createMetricAverage(3.0))
            .setPlanReviewConversionRatePercent(createMetricPercent(50.0))
            .setNewWorkspaceActivationRatePercent(createMetricPercent(100.0))
            .setGoalCompletionRatePercent(createMetricPercent(goalCompletionRatePercent))
            .build();
        WeeklyOperationMetricSnapshot weeklyOperationMetricSnapshot =
            WeeklyOperationMetricSnapshot.create(DaymarkWeekRange.of(weekStartDate, weekEndDate));
        weeklyOperationMetricSnapshot.updateFrom(
            previousWeeklyOperationsSummary,
            weekEndDate.atTime(9, 0)
        );
        mWeeklyOperationMetricSnapshotRepository.save(weeklyOperationMetricSnapshot);
    }

    private static WeeklyOperationMetricCount createMetricCount(long value) {
        return WeeklyOperationMetricCount.of(value);
    }

    private static WeeklyOperationMetricAverage createMetricAverage(double value) {
        return WeeklyOperationMetricAverage.of(value);
    }

    private static WeeklyOperationMetricPercent createMetricPercent(double value) {
        return WeeklyOperationMetricPercent.of(value);
    }
}
