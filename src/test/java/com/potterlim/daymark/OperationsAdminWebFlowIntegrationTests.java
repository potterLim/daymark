package com.potterlim.daymark;

import java.time.LocalDate;
import com.potterlim.daymark.dto.auth.RegisterUserAccountCommand;
import com.potterlim.daymark.entity.EOperationEventType;
import com.potterlim.daymark.entity.UserAccount;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OperationsAdminWebFlowIntegrationTests extends WebFlowIntegrationTestSupport {

    @Test
    void operationsDashboardShouldRequireAdministratorRole() throws Exception {
        mMockMvc.perform(get("/admin/operations")
                .with(SecurityMockMvcRequestPostProcessors.user("regular-user").roles("USER")))
            .andExpect(status().isNotFound())
            .andExpect(content().string(containsString("페이지를 찾을 수 없습니다.")));

        mMockMvc.perform(get("/admin/operations")
                .with(SecurityMockMvcRequestPostProcessors.user("operations-admin").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("운영 현황")))
            .andExpect(content().string(containsString("Weekly History")));
    }

    @Test
    void operationsDashboardShouldExcludeAdministratorActivity() throws Exception {
        savePreviousWeeklyOperationsSnapshot();

        UserAccount firstUser = mUserAccountService.registerUserAccount(
            RegisterUserAccountCommand.createFromRawInput("metrics-user-1", "metrics-user-1@example.com", "pass1234")
        );
        UserAccount secondUser = mUserAccountService.registerUserAccount(
            RegisterUserAccountCommand.createFromRawInput("metrics-user-2", "metrics-user-2@example.com", "pass1234")
        );
        UserAccount adminUser = mUserAccountService.registerUserAccount(
            RegisterUserAccountCommand.createFromRawInput("metrics-admin", "metrics-admin@example.com", "pass1234")
        );
        adminUser.grantAdministratorRole();
        mUserAccountRepository.saveAndFlush(adminUser);

        saveMorningPlan(TEST_CURRENT_DATE, firstUser, "사용자 목표 1");
        saveEveningReview(TEST_CURRENT_DATE, firstUser, createCompletedGoal("사용자 목표 1"));
        saveMorningPlan(TEST_CURRENT_DATE, secondUser, "사용자 목표 2");
        saveEveningReview(TEST_CURRENT_DATE, secondUser, createPendingGoal("사용자 목표 2"));
        saveMorningPlan(TEST_CURRENT_DATE, adminUser, "관리자 목표");
        saveEveningReview(TEST_CURRENT_DATE, adminUser, createCompletedGoal("관리자 목표"));

        mOperationUsageEventService.recordUserEvent(
            EOperationEventType.SIGN_IN_SUCCEEDED,
            firstUser.getUserAccountId()
        );
        mOperationUsageEventService.recordUserEvent(
            EOperationEventType.SIGN_IN_SUCCEEDED,
            secondUser.getUserAccountId()
        );
        mOperationUsageEventService.recordUserEvent(
            EOperationEventType.SIGN_IN_SUCCEEDED,
            adminUser.getUserAccountId()
        );
        mOperationUsageEventService.recordUserEvent(
            EOperationEventType.RECORD_LIBRARY_VIEWED,
            firstUser.getUserAccountId()
        );
        mOperationUsageEventService.recordUserEvent(
            EOperationEventType.RECORD_LIBRARY_VIEWED,
            adminUser.getUserAccountId()
        );
        mOperationUsageEventService.recordUserEvent(
            EOperationEventType.WEEKLY_REVIEW_VIEWED,
            firstUser.getUserAccountId()
        );
        mOperationUsageEventService.recordUserEvent(
            EOperationEventType.MARKDOWN_EXPORTED,
            firstUser.getUserAccountId()
        );
        mOperationUsageEventService.recordUserEvent(
            EOperationEventType.PDF_EXPORT_VIEWED,
            secondUser.getUserAccountId()
        );
        mOperationUsageEventService.recordUserEvent(
            EOperationEventType.MARKDOWN_EXPORTED,
            adminUser.getUserAccountId()
        );

        mMockMvc.perform(get("/admin/operations")
                .with(SecurityMockMvcRequestPostProcessors.user(adminUser)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("운영 현황")))
            .andExpect(content().string(containsString("Workspace Trend")))
            .andExpect(content().string(containsString("Weekly Workspace Counts")))
            .andExpect(content().string(containsString("This Week")))
            .andExpect(content().string(containsString("Completed Goals")))
            .andExpect(content().string(containsString("Exports")))
            .andExpect(content().string(containsString("Active Workspaces")))
            .andExpect(content().string(containsString("New Workspaces")))
            .andExpect(content().string(containsString("Writing Days")))
            .andExpect(content().string(containsString("Plan to Review")))
            .andExpect(content().string(containsString("Avg Writing Days")))
            .andExpect(content().string(containsString("Failed Sign-ins")))
            .andExpect(content().string(containsString("Weekly History")))
            .andExpect(content().string(containsString("50.0%")))
            .andExpect(content().string(not(containsString("66.7%"))))
            .andExpect(content().string(not(containsString("Security"))))
            .andExpect(content().string(not(containsString("Morning Plan"))))
            .andExpect(content().string(not(containsString("Weekly Review"))))
            .andExpect(content().string(not(containsString("Markdown"))))
            .andExpect(content().string(not(containsString("PDF"))))
            .andExpect(content().string(not(containsString("Routine Days"))))
            .andExpect(content().string(not(containsString("Plan → Review"))))
            .andExpect(content().string(not(containsString("Records Viewed"))))
            .andExpect(content().string(not(containsString("Quality"))))
            .andExpect(content().string(containsString("Base Date")))
            .andExpect(content().string(containsString("12W")))
            .andExpect(content().string(containsString("trend-line-active")))
            .andExpect(content().string(containsString("<span>Failed Sign-ins</span>")))
            .andExpect(content().string(containsString("Total Workspaces 2")))
            .andExpect(content().string(containsString("Writing Workspaces 2")))
            .andExpect(content().string(containsString("Exporting Workspaces 2")))
            .andExpect(content().string(not(containsString("Total Workspaces 3"))));
    }

    @Test
    void operationsDashboardShouldAllowTrendPeriodAndDateSelection() throws Exception {
        UserAccount adminUser = mUserAccountService.registerUserAccount(
            RegisterUserAccountCommand.createFromRawInput("trend-admin", "trend-admin@example.com", "pass1234")
        );
        adminUser.grantAdministratorRole();
        mUserAccountRepository.saveAndFlush(adminUser);

        saveWeeklyOperationsSnapshot(LocalDate.of(2026, 3, 9), 1L, 1L, 40.0);
        saveWeeklyOperationsSnapshot(LocalDate.of(2026, 3, 23), 3L, 2L, 58.0);
        UserAccount trendUser = mUserAccountService.registerUserAccount(
            RegisterUserAccountCommand.createFromRawInput("trend-user", "trend-user@example.com", "pass1234")
        );
        saveMorningPlan(
            LocalDate.of(2026, 4, 14),
            trendUser,
            "선택 주간 목표"
        );

        mMockMvc.perform(get("/admin/operations")
                .param("date", "2026-04-17")
                .param("weeks", "4")
                .with(SecurityMockMvcRequestPostProcessors.user(adminUser)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("value=\"2026-04-17\"")))
            .andExpect(content().string(containsString("data-auto-submit-control")))
            .andExpect(content().string(containsString("data-trend-week-input")))
            .andExpect(content().string(containsString("Trend Range")))
            .andExpect(content().string(containsString("4W")))
            .andExpect(content().string(containsString("2026. 03. 23.")))
            .andExpect(content().string(containsString("2026. 04. 13.")))
            .andExpect(content().string(containsString("2026. 04. 19.")))
            .andExpect(content().string(containsString("Total Workspaces 1")))
            .andExpect(content().string(containsString("Writing Workspaces 1")))
            .andExpect(content().string(not(containsString("03. 09."))));
    }
}
