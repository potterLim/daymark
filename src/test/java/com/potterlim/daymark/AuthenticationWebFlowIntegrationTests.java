package com.potterlim.daymark;

import com.potterlim.daymark.dto.auth.RegisterUserAccountCommand;
import com.potterlim.daymark.entity.UserAccount;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthenticationWebFlowIntegrationTests extends WebFlowIntegrationTestSupport {

    @Test
    void registerShouldShowGoogleStartWhenGoogleIdentityIsNotConfirmed() throws Exception {
        mMockMvc.perform(get("/register"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Continue with Google")))
            .andExpect(content().string(containsString("data-google-oauth-link")))
            .andExpect(content().string(containsString("외부 브라우저가 필요해요")))
            .andExpect(content().string(containsString("google-g.svg")))
            .andExpect(content().string(containsString("Google 계정 확인 후 Workspace ID를 만듭니다.")))
            .andExpect(content().string(not(containsString("사용할 워크스페이스 ID를 입력하세요"))));
    }

    @Test
    void registerShouldCreateGoogleConnectedUserAndRedirectToHome() throws Exception {
        mMockMvc.perform(post("/register")
                .session(createGoogleRegistrationSession("google-subject-1", "tester@example.com"))
                .with(csrf())
                .param("userName", "tester")
                .param("password", "pass1234")
                .param("confirmPassword", "pass1234"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"))
            .andExpect(flash().attribute("accountSuccessMessage", "Workspace가 생성되었습니다."));

        UserAccount userAccount = mUserAccountRepository.findByUserName("tester").orElseThrow();
        assertEquals("tester@example.com", userAccount.getEmailAddress());
        assertTrue(userAccount.hasVerifiedEmailAddress());
        assertTrue(userAccount.hasConnectedGoogleAccount());
    }

    @Test
    void configuredAdministratorWorkspaceShouldReceiveAdministratorRole() throws Exception {
        mMockMvc.perform(post("/register")
                .session(createGoogleRegistrationSession("google-subject-admin", "admin@example.com"))
                .with(csrf())
                .param("userName", "potterLim")
                .param("password", "pass1234")
                .param("confirmPassword", "pass1234"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));

        UserAccount userAccount = mUserAccountRepository.findByUserName("potterLim").orElseThrow();
        assertTrue(userAccount.isAdministrator());
    }

    @Test
    void administratorInitializerShouldPromoteExistingConfiguredWorkspace() {
        UserAccount userAccount = UserAccount.createRegularUser(
            "potterlim",
            "existing-admin@example.com",
            "encoded-password"
        );
        mUserAccountRepository.saveAndFlush(userAccount);

        mAdministratorAccountInitializer.promoteConfiguredAdministratorAccounts();

        UserAccount administratorUserAccount = mUserAccountRepository.findByUserName("potterlim").orElseThrow();
        assertTrue(administratorUserAccount.isAdministrator());
    }

    @Test
    void registerShouldRejectShortPassword() throws Exception {
        mMockMvc.perform(post("/register")
                .session(createGoogleRegistrationSession("google-subject-2", "tester@example.com"))
                .with(csrf())
                .param("userName", "tester")
                .param("password", "pass12")
                .param("confirmPassword", "pass12"))
            .andExpect(status().isOk());

        assertTrue(mUserAccountRepository.findByUserName("tester").isEmpty());
    }

    @Test
    void registerShouldRejectDuplicateWorkspaceId() throws Exception {
        mMockMvc.perform(post("/register")
                .session(createGoogleRegistrationSession("google-subject-3", "first@example.com"))
                .with(csrf())
                .param("userName", "privacy-user")
                .param("password", "pass1234")
                .param("confirmPassword", "pass1234"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));

        mMockMvc.perform(post("/register")
                .session(createGoogleRegistrationSession("google-subject-4", "second@example.com"))
                .with(csrf())
                .param("userName", "privacy-user")
                .param("password", "pass1234")
                .param("confirmPassword", "pass1234"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("이미 사용 중인 워크스페이스 ID입니다.")));
    }

    @Test
    void registerShouldRedirectToGoogleStartWithoutPendingGoogleIdentity() throws Exception {
        mMockMvc.perform(post("/register")
                .with(csrf())
                .param("userName", "tester")
                .param("password", "pass1234")
                .param("confirmPassword", "pass1234"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/register"));

        assertTrue(mUserAccountRepository.findByUserName("tester").isEmpty());
    }

    @Test
    void forgotPasswordShouldRedirectToSignInHelp() throws Exception {
        mMockMvc.perform(get("/forgot-password"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/sign-in-help"));
    }

    @Test
    void signInHelpShouldRenderGoogleRecoveryPath() throws Exception {
        mMockMvc.perform(get("/sign-in-help"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("로그인 도움말")))
            .andExpect(content().string(containsString("Google로 접속 후 Account에서 비밀번호를 변경하세요.")))
            .andExpect(content().string(containsString("Continue with Google")))
            .andExpect(content().string(containsString("google-g.svg")));
    }

    @Test
    void externalBrowserRequiredPageShouldRenderCopyGuidance() throws Exception {
        mMockMvc.perform(get("/external-browser-required").param("returnTo", "/register"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("외부 브라우저가 필요해요")))
            .andExpect(content().string(containsString("외부 브라우저에서 Daymark를 열어주세요.")))
            .andExpect(content().string(containsString("주소 복사하기")))
            .andExpect(content().string(containsString("http://localhost/register")));
    }

    @Test
    void googleOAuthStartShouldRedirectEmbeddedBrowserToGuidancePage() throws Exception {
        mMockMvc.perform(get("/oauth2/authorization/google")
                .header("User-Agent", "Mozilla/5.0 KAKAOTALK 10.0.0")
                .header("Referer", "https://usedaymark.com/register"))
            .andExpect(status().is3xxRedirection())
            .andExpect(header().string(
                "Location",
                containsString("/external-browser-required?returnTo=%2Fregister")
            ));
    }

    @Test
    void loginShouldShowGenericErrorWhenLoginIdentifierDoesNotExist() throws Exception {
        mMockMvc.perform(post("/login")
                .with(csrf())
                .param("loginIdentifier", "missing-user")
                .param("password", "wrong-password"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("로그인 정보가 올바르지 않습니다.")));
    }

    @Test
    void loginShouldRenderProductStyledFieldValidationMessages() throws Exception {
        mMockMvc.perform(post("/login")
                .with(csrf())
                .param("loginIdentifier", "")
                .param("password", ""))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("워크스페이스 ID 또는 이메일을 입력해주세요.")))
            .andExpect(content().string(containsString("비밀번호를 입력해주세요.")));
    }

    @Test
    void loginShouldRateLimitRepeatedAttemptsByClient() throws Exception {
        for (int attemptIndex = 0; attemptIndex < 10; attemptIndex++) {
            mMockMvc.perform(post("/login")
                    .with(csrf())
                    .param("loginIdentifier", "missing-user-" + attemptIndex)
                    .param("password", "wrong-password"))
                .andExpect(status().isOk());
        }

        mMockMvc.perform(post("/login")
                .with(csrf())
                .param("loginIdentifier", "missing-user-over-limit")
                .param("password", "wrong-password"))
            .andExpect(status().isTooManyRequests())
            .andExpect(content().string(containsString("요청이 잠시 제한되었습니다.")));
    }

    @Test
    void loginPageShouldCanonicalizeUnexpectedQueryString() throws Exception {
        mMockMvc.perform(get("/login?next=/daymark/preview?date=" + TEST_CURRENT_DATE))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
    }

    @Test
    void loginShouldRedirectToSavedProductPathAfterAuthentication() throws Exception {
        mUserAccountService.registerUserAccount(
            new RegisterUserAccountCommand("next-user", "next-user@example.com", "pass1234")
        );

        MvcResult protectedPageResult = mMockMvc.perform(get("/daymark/preview?date=" + TEST_CURRENT_DATE))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("http://localhost/login"))
            .andReturn();
        MockHttpSession session = (MockHttpSession) protectedPageResult.getRequest().getSession(false);
        assertNotNull(session);

        mMockMvc.perform(post("/login")
                .session(session)
                .with(csrf())
                .param("loginIdentifier", "next-user")
                .param("password", "pass1234"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/daymark/preview?date=" + TEST_CURRENT_DATE));
    }

    @Test
    void loginShouldIgnoreUnexpectedNextPathParameterAfterAuthentication() throws Exception {
        mUserAccountService.registerUserAccount(
            new RegisterUserAccountCommand("safe-next-user", "safe-next-user@example.com", "pass1234")
        );

        mMockMvc.perform(post("/login")
                .with(csrf())
                .param("loginIdentifier", "safe-next-user")
                .param("password", "pass1234")
                .param("nextPath", "https://example.com/daymark/preview"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));
    }

    @Test
    void loginShouldShowGenericErrorWhenPasswordIsWrong() throws Exception {
        mUserAccountService.registerUserAccount(
            new RegisterUserAccountCommand("tester", "tester@example.com", "pass1234")
        );

        mMockMvc.perform(post("/login")
                .with(csrf())
                .param("loginIdentifier", "tester")
                .param("password", "wrong-password")
                .param("rememberMe", "true"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("로그인 정보가 올바르지 않습니다.")));
    }

    @Test
    void loginShouldAcceptEmailAddress() throws Exception {
        mUserAccountService.registerUserAccount(
            new RegisterUserAccountCommand("tester", "tester@example.com", "pass1234")
        );

        mMockMvc.perform(post("/login")
                .with(csrf())
                .param("loginIdentifier", "tester@example.com")
                .param("password", "pass1234"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));
    }

    @Test
    void changePasswordShouldPersistNewPassword() throws Exception {
        UserAccount userAccount = mUserAccountService.registerUserAccount(
            new RegisterUserAccountCommand("changer", "changer@example.com", "pass1234")
        );

        mMockMvc.perform(post("/account/password")
                .with(csrf())
                .with(SecurityMockMvcRequestPostProcessors.user(userAccount))
                .param("currentPassword", "pass1234")
                .param("newPassword", "pass6789")
                .param("confirmNewPassword", "pass6789"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/account/password"));

        mMockMvc.perform(post("/login")
                .with(csrf())
                .param("loginIdentifier", "changer")
                .param("password", "pass1234"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("로그인 정보가 올바르지 않습니다.")));

        mMockMvc.perform(post("/login")
                .with(csrf())
                .param("loginIdentifier", "changer")
                .param("password", "pass6789"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));
    }

    @Test
    void changePasswordShouldRenderProductStyledFieldValidationMessages() throws Exception {
        UserAccount userAccount = mUserAccountService.registerUserAccount(
            new RegisterUserAccountCommand("blank-changer", "blank-changer@example.com", "pass1234")
        );

        mMockMvc.perform(post("/account/password")
                .with(csrf())
                .with(SecurityMockMvcRequestPostProcessors.user(userAccount))
                .param("currentPassword", "")
                .param("newPassword", "")
                .param("confirmNewPassword", ""))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("현재 비밀번호를 입력해주세요.")))
            .andExpect(content().string(containsString("새 비밀번호를 입력해주세요.")))
            .andExpect(content().string(containsString("새 비밀번호 확인을 입력해주세요.")));
    }
}
