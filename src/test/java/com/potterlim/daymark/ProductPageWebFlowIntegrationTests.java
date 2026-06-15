package com.potterlim.daymark;

import java.time.LocalDate;
import java.util.List;
import com.potterlim.daymark.dto.auth.RegisterUserAccountCommand;
import com.potterlim.daymark.entity.UserAccount;
import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductPageWebFlowIntegrationTests extends WebFlowIntegrationTestSupport {

    @Test
    void libraryShouldSearchTimelineAndExportSelectedRecords() throws Exception {
        UserAccount userAccount = mUserAccountService.registerUserAccount(
            RegisterUserAccountCommand.createFromRawInput("library-reviewer", "library-reviewer@example.com", "pass1234")
        );
        LocalDate matchingDate = LocalDate.of(2026, 4, 22);
        LocalDate otherDate = LocalDate.of(2026, 4, 24);

        saveMorningPlan(
            matchingDate,
            userAccount,
            "검색 가능한 제품 흐름 점검"
        );
        saveEveningReview(
            matchingDate,
            userAccount,
            List.of(createCompletedGoal("검색 가능한 제품 흐름 점검")),
            "검색 가능한 성과를 남겼다.",
            "",
            "",
            ""
        );
        saveMorningPlan(
            otherDate,
            userAccount,
            "온보딩 메모 정리"
        );

        mMockMvc.perform(get("/daymark/library")
                .with(SecurityMockMvcRequestPostProcessors.user(userAccount))
                .param("from", "2026-04-20")
                .param("to", "2026-04-24")
                .param("keyword", "제품 흐름"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("기록 라이브러리")))
            .andExpect(content().string(containsString("타임라인")))
            .andExpect(content().string(containsString("목표 완료율 추이")))
            .andExpect(content().string(containsString("Preview PDF")))
            .andExpect(content().string(containsString("오늘의 목표")))
            .andExpect(content().string(containsString("검색 가능한 제품 흐름 점검")))
            .andExpect(content().string(containsString("검색 가능한 성과를 남겼다.")))
            .andExpect(content().string(not(containsString("온보딩 메모 정리"))));

        mMockMvc.perform(get("/daymark/library/export/markdown")
                .with(SecurityMockMvcRequestPostProcessors.user(userAccount))
                .param("from", "2026-04-20")
                .param("to", "2026-04-24")
                .param("keyword", "제품 흐름"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("attachment")))
            .andExpect(content().string(containsString("# Daymark 기록 라이브러리")))
            .andExpect(content().string(containsString("검색 가능한 제품 흐름 점검")))
            .andExpect(content().string(not(containsString("온보딩 메모 정리"))));

        mMockMvc.perform(get("/daymark/library/export/pdf")
                .with(SecurityMockMvcRequestPostProcessors.user(userAccount))
                .param("from", "2026-04-20")
                .param("to", "2026-04-24")
                .param("keyword", "제품 흐름"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("PDF 리포트 미리보기")))
            .andExpect(content().string(containsString("완료율")))
            .andExpect(content().string(containsString("선택한 기간")))
            .andExpect(content().string(containsString("Save PDF")))
            .andExpect(content().string(containsString("검색 가능한 성과를 남겼다.")));
    }

    @Test
    void coreProductPagesShouldRender() throws Exception {
        UserAccount userAccount = mUserAccountService.registerUserAccount(
            RegisterUserAccountCommand.createFromRawInput("reviewer", "reviewer@example.com", "pass1234")
        );

        mMockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Daymark")))
            .andExpect(content().string(containsString("Sign In")))
            .andExpect(content().string(containsString("Create Account")))
            .andExpect(content().string(containsString("Contact:")))
            .andExpect(content().string(containsString("potterLim0808@gmail.com")))
            .andExpect(content().string(containsString("/daymark/morning/edit?date=" + TEST_CURRENT_DATE)))
            .andExpect(content().string(containsString("/daymark/evening/edit?date=" + TEST_CURRENT_DATE)))
            .andExpect(content().string(containsString("/daymark/preview?date=" + TEST_CURRENT_DATE)))
            .andExpect(content().string(not(containsString("/login?next="))))
            .andExpect(content().string(not(containsString("Sign Out"))));

        mMockMvc.perform(get("/")
            .with(SecurityMockMvcRequestPostProcessors.user(userAccount)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Today")))
            .andExpect(content().string(containsString("Daymark")))
            .andExpect(content().string(containsString("aria-label=\"Daymark\"")))
            .andExpect(content().string(containsString("daymark-logo.svg")))
            .andExpect(content().string(containsString("Private Workspace")))
            .andExpect(content().string(not(containsString("Local-first"))))
            .andExpect(content().string(not(containsString("하단 메뉴"))))
            .andExpect(content().string(containsString("Plan")))
            .andExpect(content().string(containsString("Check")))
            .andExpect(content().string(containsString("Keep")))
            .andExpect(content().string(containsString("Review")))
            .andExpect(content().string(containsString("View Today")))
            .andExpect(content().string(containsString("/daymark/morning/edit?date=" + TEST_CURRENT_DATE)))
            .andExpect(content().string(containsString("/daymark/evening/edit?date=" + TEST_CURRENT_DATE)))
            .andExpect(content().string(containsString("/daymark/preview?date=" + TEST_CURRENT_DATE)));

        mMockMvc.perform(get("/daymark/morning")
            .with(SecurityMockMvcRequestPostProcessors.user(userAccount)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("New Plan")))
            .andExpect(content().string(containsString("/daymark/morning/edit?date=" + TEST_CURRENT_DATE)));

        mMockMvc.perform(get("/daymark/morning/edit")
                .with(SecurityMockMvcRequestPostProcessors.user(userAccount))
                .param("date", TEST_CURRENT_DATE.toString()))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("한 줄에 하나씩 목표를 적어주세요")))
            .andExpect(content().string(containsString("예: 출시 전 화면 점검")))
            .andExpect(content().string(not(containsString("field-guide"))));

        mMockMvc.perform(get("/daymark/evening")
                .with(SecurityMockMvcRequestPostProcessors.user(userAccount)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("저녁 회고")))
            .andExpect(content().string(containsString("Open Review")))
            .andExpect(content().string(containsString("New Review")))
            .andExpect(content().string(not(containsString("주 이동"))));

        mMockMvc.perform(get("/daymark/evening/edit")
                .with(SecurityMockMvcRequestPostProcessors.user(userAccount))
                .param("date", TEST_CURRENT_DATE.toString()))
            .andExpect(status().isOk())
            .andExpect(content().string(not(containsString("<span class=\"content-badge\">읽기 전용</span>"))))
            .andExpect(content().string(containsString("아침 계획이 없습니다.")))
            .andExpect(content().string(not(containsString("아침 기록가 없습니다."))));

        mMockMvc.perform(get("/daymark/week")
            .with(SecurityMockMvcRequestPostProcessors.user(userAccount)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("주간 리뷰")))
            .andExpect(content().string(containsString("완료율")))
            .andExpect(content().string(containsString("Prev")))
            .andExpect(content().string(containsString("Current")))
            .andExpect(content().string(containsString("Next")))
            .andExpect(content().string(containsString("New Plan")));

        mMockMvc.perform(get("/daymark/library")
                .with(SecurityMockMvcRequestPostProcessors.user(userAccount)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("기록 라이브러리")))
            .andExpect(content().string(containsString("Download MD")))
            .andExpect(content().string(containsString("Preview PDF")));

        mMockMvc.perform(get("/account/password")
                .with(SecurityMockMvcRequestPostProcessors.user(userAccount)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("비밀번호 변경")))
            .andExpect(content().string(containsString("8자 이상 72자 이하")));

        mMockMvc.perform(get("/account")
                .with(SecurityMockMvcRequestPostProcessors.user(userAccount)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("계정")))
            .andExpect(content().string(containsString("가입 Google 메일")))
            .andExpect(content().string(containsString("Workspace ID")))
            .andExpect(content().string(containsString("Workspace 생성일")))
            .andExpect(content().string(containsString("reviewer")))
            .andExpect(content().string(containsString("reviewer@example.com")))
            .andExpect(content().string(not(containsString(">Google</span>"))))
            .andExpect(content().string(not(containsString("연결됨"))))
            .andExpect(content().string(containsString("Change Password")));

        mMockMvc.perform(get("/images/daymark-logo.svg"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("daymark-bg")))
            .andExpect(content().string(containsString("rounded blue calendar mark")));
    }

    @Test
    void logoutShouldReturnToPublicHome() throws Exception {
        UserAccount userAccount = mUserAccountService.registerUserAccount(
            RegisterUserAccountCommand.createFromRawInput("logout-reviewer", "logout-reviewer@example.com", "pass1234")
        );

        mMockMvc.perform(post("/logout")
                .with(csrf())
                .with(SecurityMockMvcRequestPostProcessors.user(userAccount)))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/?logout"));

        mMockMvc.perform(get("/").param("logout", ""))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("로그아웃되었습니다.")))
            .andExpect(content().string(containsString("Sign In")))
            .andExpect(content().string(containsString("Create Account")))
            .andExpect(content().string(not(containsString("Sign Out"))));
    }

    @Test
    void missingProductPageShouldRenderFriendlyNotFoundPage() throws Exception {
        UserAccount userAccount = mUserAccountService.registerUserAccount(
            RegisterUserAccountCommand.createFromRawInput("missing-page-reviewer", "missing-page-reviewer@example.com", "pass1234")
        );

        mMockMvc.perform(get("/missing-public-page"))
            .andExpect(status().isNotFound())
            .andExpect(content().string(containsString("페이지를 찾을 수 없습니다.")))
            .andExpect(content().string(containsString("Home")))
            .andExpect(content().string(containsString("potterLim0808@gmail.com")))
            .andExpect(content().string(not(containsString("Library"))));

        mMockMvc.perform(get("/missing-product-page")
            .with(SecurityMockMvcRequestPostProcessors.user(userAccount)))
            .andExpect(status().isNotFound())
            .andExpect(content().string(containsString("페이지를 찾을 수 없습니다.")))
            .andExpect(content().string(containsString("Home")))
            .andExpect(content().string(containsString("potterLim0808@gmail.com")))
            .andExpect(content().string(not(containsString("Library"))));
    }

    @Test
    void malformedProductRequestsShouldRenderNotFoundPage() throws Exception {
        UserAccount userAccount = mUserAccountService.registerUserAccount(
            RegisterUserAccountCommand.createFromRawInput("malformed-user", "malformed-user@example.com", "pass1234")
        );

        mMockMvc.perform(get("/daymark/morning/edit")
                .with(SecurityMockMvcRequestPostProcessors.user(userAccount)))
            .andExpect(status().isNotFound())
            .andExpect(content().string(containsString("페이지를 찾을 수 없습니다.")))
            .andExpect(content().string(not(containsString("timestamp"))));

        mMockMvc.perform(get("/daymark/morning/edit")
                .param("date", "not-a-date")
                .with(SecurityMockMvcRequestPostProcessors.user(userAccount)))
            .andExpect(status().isNotFound())
            .andExpect(content().string(containsString("페이지를 찾을 수 없습니다.")))
            .andExpect(content().string(not(containsString("MethodArgumentTypeMismatchException"))));
    }

    @Test
    void errorEndpointShouldRenderNotFoundPageWithoutDiagnosticDetails() throws Exception {
        mMockMvc.perform(get("/error")
                .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 500)
                .requestAttr(
                    RequestDispatcher.ERROR_EXCEPTION,
                    new IllegalStateException("internal-secret")
                ))
            .andExpect(status().isNotFound())
            .andExpect(content().string(containsString("페이지를 찾을 수 없습니다.")))
            .andExpect(content().string(not(containsString("internal-secret"))))
            .andExpect(content().string(not(containsString("IllegalStateException"))));
    }

    @Test
    void protectedProductPagesShouldStillRequireLogin() throws Exception {
        for (String protectedPath : new String[] {
            "/account",
            "/account/password",
            "/account/unmapped",
            "/admin/operations",
            "/admin/unmapped",
            "/daymark/morning",
            "/daymark/morning/edit",
            "/daymark/evening",
            "/daymark/evening/edit",
            "/daymark/week",
            "/daymark/library",
            "/daymark/library/export/markdown",
            "/daymark/library/export/pdf",
            "/daymark/preview",
            "/daymark/unmapped"
        }) {
            mMockMvc.perform(get(protectedPath))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
        }
    }

    @Test
    void healthEndpointShouldBePublicAndUp() throws Exception {
        mMockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("\"status\":\"UP\"")));
    }
}
