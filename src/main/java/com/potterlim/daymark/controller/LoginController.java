package com.potterlim.daymark.controller;

import com.potterlim.daymark.dto.auth.LoginRequestDto;
import com.potterlim.daymark.entity.EOperationEventType;
import com.potterlim.daymark.entity.UserAccount;
import com.potterlim.daymark.security.ApplicationAuthenticationService;
import com.potterlim.daymark.security.PostLoginRedirectPathResolver;
import com.potterlim.daymark.service.IUserAccountService;
import com.potterlim.daymark.service.OperationUsageEventService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LoginController {

    private final IUserAccountService mUserAccountService;
    private final ApplicationAuthenticationService mApplicationAuthenticationService;
    private final RememberMeServices mRememberMeServices;
    private final PostLoginRedirectPathResolver mPostLoginRedirectPathResolver;
    private final OperationUsageEventService mOperationUsageEventService;

    public LoginController(
        IUserAccountService userAccountService,
        ApplicationAuthenticationService applicationAuthenticationService,
        RememberMeServices rememberMeServices,
        PostLoginRedirectPathResolver postLoginRedirectPathResolver,
        OperationUsageEventService operationUsageEventService
    ) {
        mUserAccountService = userAccountService;
        mApplicationAuthenticationService = applicationAuthenticationService;
        mRememberMeServices = rememberMeServices;
        mPostLoginRedirectPathResolver = postLoginRedirectPathResolver;
        mOperationUsageEventService = operationUsageEventService;
    }

    @GetMapping("/login")
    public String showLoginPage(Authentication authentication, HttpServletRequest httpServletRequest, Model model) {
        if (AuthenticationViewSupport.isAuthenticated(authentication)) {
            return "redirect:/";
        }

        String googleStateOrNull = httpServletRequest.getParameter("google");
        if (httpServletRequest.getQueryString() != null && googleStateOrNull == null) {
            return "redirect:/login";
        }

        if ("failed".equals(googleStateOrNull)) {
            model.addAttribute("loginErrorMessage", "Google 로그인에 실패했습니다. 잠시 후 다시 시도해주세요.");
        } else if ("unverified".equals(googleStateOrNull)) {
            model.addAttribute("loginErrorMessage", "Google에서 확인된 이메일 계정만 사용할 수 있습니다.");
        }

        if (!model.containsAttribute("loginRequestDto")) {
            model.addAttribute("loginRequestDto", new LoginRequestDto());
        }

        return "auth/login";
    }

    @PostMapping("/login")
    public String login(
        @Valid @ModelAttribute("loginRequestDto") LoginRequestDto loginRequestDto,
        BindingResult bindingResult,
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse,
        Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/login";
        }

        String normalizedLoginIdentifier = loginRequestDto.getLoginIdentifier().trim();
        Authentication authentication;

        try {
            if (loginRequestDto.isRememberMe()) {
                authentication = authenticateUserWithRememberMe(
                    normalizedLoginIdentifier,
                    loginRequestDto.getPassword(),
                    httpServletRequest,
                    httpServletResponse
                );
            } else {
                authentication = mApplicationAuthenticationService.authenticateWithPassword(
                    normalizedLoginIdentifier,
                    loginRequestDto.getPassword(),
                    httpServletRequest,
                    httpServletResponse
                );
            }
        } catch (AuthenticationException authenticationException) {
            mRememberMeServices.loginFail(httpServletRequest, httpServletResponse);
            recordFailedSignInEvent(normalizedLoginIdentifier);
            model.addAttribute("loginErrorMessage", "로그인 정보가 올바르지 않습니다.");
            return "auth/login";
        }

        recordAuthenticatedUserEvent(EOperationEventType.SIGN_IN_SUCCEEDED, authentication);
        return "redirect:" + mPostLoginRedirectPathResolver.resolveSavedRequestRedirectPath(
            httpServletRequest,
            httpServletResponse
        );
    }

    private Authentication authenticateUserWithRememberMe(
        String userName,
        String rawPassword,
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse
    ) {
        Authentication authentication = mApplicationAuthenticationService.authenticateWithPassword(
            userName,
            rawPassword,
            httpServletRequest,
            httpServletResponse
        );

        mRememberMeServices.loginSuccess(httpServletRequest, httpServletResponse, authentication);
        return authentication;
    }

    private void recordAuthenticatedUserEvent(EOperationEventType eventType, Authentication authentication) {
        if (authentication.getPrincipal() instanceof UserAccount userAccount) {
            mOperationUsageEventService.recordUserEvent(eventType, userAccount.getUserAccountId());
            return;
        }

        mOperationUsageEventService.recordAnonymousEvent(eventType);
    }

    private void recordFailedSignInEvent(String loginIdentifier) {
        mUserAccountService.findUserAccountByLoginIdentifier(loginIdentifier)
            .ifPresentOrElse(
                userAccount -> mOperationUsageEventService.recordUserEvent(
                    EOperationEventType.SIGN_IN_FAILED,
                    userAccount.getUserAccountId()
                ),
                () -> mOperationUsageEventService.recordAnonymousEvent(EOperationEventType.SIGN_IN_FAILED)
            );
    }
}
