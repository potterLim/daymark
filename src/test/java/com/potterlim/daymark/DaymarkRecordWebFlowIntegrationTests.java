package com.potterlim.daymark;

import java.time.LocalDate;
import com.potterlim.daymark.dto.auth.RegisterUserAccountCommand;
import com.potterlim.daymark.entity.UserAccount;
import com.potterlim.daymark.support.EDaymarkSectionType;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DaymarkRecordWebFlowIntegrationTests extends WebFlowIntegrationTestSupport {

    @Test
    void morningSaveShouldPersistDaymarkEntry() throws Exception {
        UserAccount userAccount = mUserAccountService.registerUserAccount(
            new RegisterUserAccountCommand("writer", "writer@example.com", "pass1234")
        );

        mMockMvc.perform(post("/daymark/morning/save")
                .with(csrf())
                .with(SecurityMockMvcRequestPostProcessors.user(userAccount))
                .param("date", "2026-04-01")
                .param("goals", "운동하기\n책 읽기")
                .param("focus", "집중 업무")
                .param("challenges", "피곤함 관리"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/daymark/morning"));

        String markdownText = mDaymarkService.readEntryMarkdownContent(LocalDate.of(2026, 4, 1), userAccount.getUserAccountId());

        assertTrue(markdownText.contains("## 오늘의 목표"));
        assertTrue(markdownText.contains("- 운동하기"));
        assertTrue(markdownText.contains("- 책 읽기"));
        assertTrue(markdownText.contains("## 집중 영역"));
        assertEquals(1, mDaymarkEntryRepository.count());
    }

    @Test
    void morningSaveShouldRejectOversizedInput() throws Exception {
        UserAccount userAccount = mUserAccountService.registerUserAccount(
            new RegisterUserAccountCommand("oversized-writer", "oversized-writer@example.com", "pass1234")
        );

        mMockMvc.perform(post("/daymark/morning/save")
                .with(csrf())
                .with(SecurityMockMvcRequestPostProcessors.user(userAccount))
                .param("date", TEST_CURRENT_DATE.toString())
                .param("goals", "가".repeat(301))
                .param("focus", "집중 업무")
                .param("challenges", "피곤함 관리"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("목표는 한 줄당 300자 이하로 입력해주세요.")));

        assertEquals(0, mDaymarkEntryRepository.count());
    }

    @Test
    void eveningSaveShouldRejectOversizedInput() throws Exception {
        UserAccount userAccount = mUserAccountService.registerUserAccount(
            new RegisterUserAccountCommand("oversized-reviewer", "oversized-reviewer@example.com", "pass1234")
        );

        mMockMvc.perform(post("/daymark/evening/save")
                .with(csrf())
                .with(SecurityMockMvcRequestPostProcessors.user(userAccount))
                .param("date", TEST_CURRENT_DATE.toString())
                .param("achievements", "성과")
                .param("improvements", "개선")
                .param("gratitude", "감사".repeat(501))
                .param("notes", "메모"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("감사는 1,000자 이하로 입력해주세요.")));

        assertEquals(0, mDaymarkEntryRepository.count());
    }

    @Test
    void blankMorningSaveShouldNotCreateVisibleDaymarkEntry() throws Exception {
        UserAccount userAccount = mUserAccountService.registerUserAccount(
            new RegisterUserAccountCommand("blank-writer", "blank-writer@example.com", "pass1234")
        );

        mMockMvc.perform(post("/daymark/morning/save")
                .with(csrf())
                .with(SecurityMockMvcRequestPostProcessors.user(userAccount))
                .param("date", TEST_CURRENT_DATE.toString())
                .param("goals", "   ")
                .param("focus", "")
                .param("challenges", "\n"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/daymark/morning"));

        assertEquals(0, mDaymarkEntryRepository.count());

        mMockMvc.perform(get("/daymark/morning")
                .with(SecurityMockMvcRequestPostProcessors.user(userAccount)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("아직 저장된 계획이 없습니다.")))
            .andExpect(content().string(not(containsString("계획 수정"))));

        mMockMvc.perform(get("/daymark/week")
                .with(SecurityMockMvcRequestPostProcessors.user(userAccount)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("이번 주 기록이 없습니다.")))
            .andExpect(content().string(not(containsString("0 / 0 완료"))));

        mMockMvc.perform(get("/daymark/preview")
                .with(SecurityMockMvcRequestPostProcessors.user(userAccount))
                .param("date", TEST_CURRENT_DATE.toString()))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("아직 저장된 기록이 없습니다.")))
            .andExpect(content().string(containsString("New Plan")))
            .andExpect(content().string(not(containsString("오늘의 목표"))));
    }

    @Test
    void clearingMorningSectionsShouldRemoveDaymarkEntry() throws Exception {
        UserAccount userAccount = mUserAccountService.registerUserAccount(
            new RegisterUserAccountCommand("clear-writer", "clear-writer@example.com", "pass1234")
        );

        mMockMvc.perform(post("/daymark/morning/save")
                .with(csrf())
                .with(SecurityMockMvcRequestPostProcessors.user(userAccount))
                .param("date", TEST_CURRENT_DATE.toString())
                .param("goals", "운동하기")
                .param("focus", "집중 업무")
                .param("challenges", "피곤함 관리"))
            .andExpect(status().is3xxRedirection());

        assertEquals(1, mDaymarkEntryRepository.count());

        mMockMvc.perform(post("/daymark/morning/save")
                .with(csrf())
                .with(SecurityMockMvcRequestPostProcessors.user(userAccount))
                .param("date", TEST_CURRENT_DATE.toString())
                .param("goals", "")
                .param("focus", "")
                .param("challenges", ""))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/daymark/morning"));

        assertEquals(0, mDaymarkEntryRepository.count());
    }

    @Test
    void previewShouldOmitEmptySectionHeaders() throws Exception {
        UserAccount userAccount = mUserAccountService.registerUserAccount(
            new RegisterUserAccountCommand("preview-writer", "preview-writer@example.com", "pass1234")
        );

        mMockMvc.perform(post("/daymark/morning/save")
                .with(csrf())
                .with(SecurityMockMvcRequestPostProcessors.user(userAccount))
                .param("date", TEST_CURRENT_DATE.toString())
                .param("goals", "운동하기")
                .param("focus", "")
                .param("challenges", ""))
            .andExpect(status().is3xxRedirection());

        mMockMvc.perform(get("/daymark/preview")
                .with(SecurityMockMvcRequestPostProcessors.user(userAccount))
                .param("date", TEST_CURRENT_DATE.toString()))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("2026. 04. 24.")))
            .andExpect(content().string(not(containsString("읽기 전용"))))
            .andExpect(content().string(containsString("오늘의 목표")))
            .andExpect(content().string(containsString("운동하기")))
            .andExpect(content().string(not(containsString("집중 영역"))))
            .andExpect(content().string(not(containsString("예상 변수"))));
    }

    @Test
    void morningListShouldRenderSavedDate() throws Exception {
        LocalDate morningDate = LocalDate.now(mClock);
        UserAccount userAccount = mUserAccountService.registerUserAccount(
            new RegisterUserAccountCommand("planner", "planner@example.com", "pass1234")
        );

        mMockMvc.perform(post("/daymark/morning/save")
                .with(csrf())
                .with(SecurityMockMvcRequestPostProcessors.user(userAccount))
                .param("date", morningDate.toString())
                .param("goals", "운동하기")
                .param("focus", "중요 업무")
                .param("challenges", "집중 유지"))
            .andExpect(status().is3xxRedirection());

        mMockMvc.perform(get("/daymark/morning")
                .with(SecurityMockMvcRequestPostProcessors.user(userAccount)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("2026. 04. 24.")));
    }

    @Test
    void eveningPageShouldRenderDirectDateSelectionWithoutWeekNavigation() throws Exception {
        UserAccount userAccount = mUserAccountService.registerUserAccount(
            new RegisterUserAccountCommand("evening-reviewer", "evening-reviewer@example.com", "pass1234")
        );

        mMockMvc.perform(get("/daymark/evening")
                .with(SecurityMockMvcRequestPostProcessors.user(userAccount)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("저녁 회고")))
            .andExpect(content().string(containsString("날짜 선택")))
            .andExpect(content().string(containsString("Open Review")))
            .andExpect(content().string(containsString("/daymark/evening/edit?date=" + TEST_CURRENT_DATE)))
            .andExpect(content().string(not(containsString("주 이동"))))
            .andExpect(content().string(not(containsString("Prev"))))
            .andExpect(content().string(not(containsString("Current"))))
            .andExpect(content().string(not(containsString("Next"))));
    }

    @Test
    void weekPageShouldNavigateBetweenWeeks() throws Exception {
        UserAccount userAccount = mUserAccountService.registerUserAccount(
            new RegisterUserAccountCommand("week-reviewer", "week-reviewer@example.com", "pass1234")
        );
        LocalDate previousWeekDate = LocalDate.of(2026, 4, 13);

        mDaymarkService.writeSection(
            previousWeekDate,
            userAccount.getUserAccountId(),
            EDaymarkSectionType.GOALS,
            "- 이전 주 목표"
        );

        mMockMvc.perform(get("/daymark/week")
                .param("week", "-1")
                .with(SecurityMockMvcRequestPostProcessors.user(userAccount)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("2026. 04. 13. ~ 2026. 04. 19.")))
            .andExpect(content().string(containsString("2026. 04. 13.")))
            .andExpect(content().string(containsString("Prev")))
            .andExpect(content().string(containsString("Current")))
            .andExpect(content().string(containsString("Next")))
            .andExpect(content().string(containsString("week-current-link")))
            .andExpect(content().string(not(containsString("week-nav-card-current"))))
            .andExpect(content().string(not(containsString("week-nav-trio"))))
            .andExpect(content().string(containsString("2026. 04. 06. ~ 2026. 04. 12.")))
            .andExpect(content().string(containsString("2026. 04. 20. ~ 2026. 04. 26.")))
            .andExpect(content().string(containsString("/daymark/week?week=-2")))
            .andExpect(content().string(containsString("/daymark/week")))
            .andExpect(content().string(containsString("/daymark/week?week=0")));
    }
}
