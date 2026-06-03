package com.potterlim.daymark;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import com.potterlim.daymark.dto.auth.GoogleRegistrationSession;
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
import com.potterlim.daymark.service.WeeklyOperationsSummary;
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

    protected void saveWeeklyOperationsSnapshot(
        LocalDate weekStartDate,
        long weeklyActiveUsers,
        long weeklyWritingUsers,
        double goalCompletionRatePercent
    ) {
        LocalDate weekEndDate = weekStartDate.plusDays(6L);
        WeeklyOperationsSummary previousWeeklyOperationsSummary = new WeeklyOperationsSummary(
            weekStartDate,
            weekEndDate,
            3L,
            1L,
            weeklyActiveUsers,
            weeklyWritingUsers,
            2L,
            2L,
            1L,
            1L,
            2L,
            1L,
            1L,
            1L,
            1L,
            0L,
            1L,
            1L,
            2.0,
            3.0,
            50.0,
            100.0,
            goalCompletionRatePercent
        );
        WeeklyOperationMetricSnapshot weeklyOperationMetricSnapshot =
            WeeklyOperationMetricSnapshot.create(weekStartDate, weekEndDate);
        weeklyOperationMetricSnapshot.updateFrom(
            previousWeeklyOperationsSummary,
            weekEndDate.atTime(9, 0)
        );
        mWeeklyOperationMetricSnapshotRepository.save(weeklyOperationMetricSnapshot);
    }
}
