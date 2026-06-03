package com.potterlim.daymark.controller;

import com.potterlim.daymark.dto.auth.GoogleRegistrationSession;
import com.potterlim.daymark.dto.auth.RegisterGoogleUserAccountCommand;
import com.potterlim.daymark.dto.auth.RegisterRequestDto;
import com.potterlim.daymark.entity.EOperationEventType;
import com.potterlim.daymark.entity.UserAccount;
import com.potterlim.daymark.security.ApplicationAuthenticationService;
import com.potterlim.daymark.service.DuplicateEmailException;
import com.potterlim.daymark.service.DuplicateUserNameException;
import com.potterlim.daymark.service.IUserAccountService;
import com.potterlim.daymark.service.OperationUsageEventService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegistrationController {

    private final IUserAccountService mUserAccountService;
    private final ApplicationAuthenticationService mApplicationAuthenticationService;
    private final OperationUsageEventService mOperationUsageEventService;

    public RegistrationController(
        IUserAccountService userAccountService,
        ApplicationAuthenticationService applicationAuthenticationService,
        OperationUsageEventService operationUsageEventService
    ) {
        mUserAccountService = userAccountService;
        mApplicationAuthenticationService = applicationAuthenticationService;
        mOperationUsageEventService = operationUsageEventService;
    }

    @GetMapping("/register")
    public String showRegisterPage(
        Authentication authentication,
        HttpServletRequest httpServletRequest,
        Model model
    ) {
        if (AuthenticationViewSupport.isAuthenticated(authentication)) {
            return "redirect:/";
        }

        if (!model.containsAttribute("registerRequestDto")) {
            model.addAttribute("registerRequestDto", new RegisterRequestDto());
        }

        prepareRegisterModel(httpServletRequest, model);
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
        @Valid @ModelAttribute("registerRequestDto") RegisterRequestDto registerRequestDto,
        BindingResult bindingResult,
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        GoogleRegistrationSession googleRegistrationSessionOrNull =
            findPendingGoogleRegistrationSessionOrNull(httpServletRequest);
        if (googleRegistrationSessionOrNull == null) {
            return "redirect:/register";
        }

        if (!registerRequestDto.hasMatchingPassword()) {
            bindingResult.rejectValue(
                "confirmPassword",
                "register.confirmPassword",
                "비밀번호와 비밀번호 확인이 일치하지 않습니다."
            );
        }

        if (bindingResult.hasErrors()) {
            prepareRegisterModel(httpServletRequest, model);
            return "auth/register";
        }

        RegisterGoogleUserAccountCommand registerGoogleUserAccountCommand =
            new RegisterGoogleUserAccountCommand(
                registerRequestDto.getUserName().trim(),
                googleRegistrationSessionOrNull.emailAddress(),
                googleRegistrationSessionOrNull.googleSubject(),
                registerRequestDto.getPassword()
            );

        UserAccount userAccount;

        try {
            userAccount = mUserAccountService.registerGoogleUserAccount(registerGoogleUserAccountCommand);
        } catch (DuplicateUserNameException duplicateUserNameException) {
            bindingResult.rejectValue(
                "userName",
                "register.userName",
                "이미 사용 중인 워크스페이스 ID입니다."
            );
            prepareRegisterModel(httpServletRequest, model);
            return "auth/register";
        } catch (DuplicateEmailException duplicateEmailException) {
            bindingResult.reject(
                "register.emailAddress",
                "이미 연결된 Google 계정입니다. 로그인으로 계속해주세요."
            );
            prepareRegisterModel(httpServletRequest, model);
            return "auth/register";
        }

        mOperationUsageEventService.recordUserEvent(
            EOperationEventType.USER_REGISTERED,
            userAccount.getUserAccountId()
        );

        mApplicationAuthenticationService.authenticateWithPassword(
            registerRequestDto.getUserName().trim(),
            registerRequestDto.getPassword(),
            httpServletRequest,
            httpServletResponse
        );

        httpServletRequest.getSession().removeAttribute(GoogleRegistrationSession.SESSION_ATTRIBUTE_NAME);
        redirectAttributes.addFlashAttribute("accountSuccessMessage", "Workspace가 생성되었습니다.");
        return "redirect:/";
    }

    private static void prepareRegisterModel(HttpServletRequest httpServletRequest, Model model) {
        GoogleRegistrationSession googleRegistrationSessionOrNull =
            findPendingGoogleRegistrationSessionOrNull(httpServletRequest);
        model.addAttribute("hasPendingGoogleRegistration", googleRegistrationSessionOrNull != null);
        if (googleRegistrationSessionOrNull == null) {
            return;
        }

        model.addAttribute("googleEmailAddress", googleRegistrationSessionOrNull.emailAddress());
        model.addAttribute("googleDisplayName", googleRegistrationSessionOrNull.displayName());
    }

    private static GoogleRegistrationSession findPendingGoogleRegistrationSessionOrNull(
        HttpServletRequest httpServletRequest
    ) {
        Object sessionValueOrNull = httpServletRequest.getSession(true)
            .getAttribute(GoogleRegistrationSession.SESSION_ATTRIBUTE_NAME);
        if (sessionValueOrNull instanceof GoogleRegistrationSession googleRegistrationSession) {
            return googleRegistrationSession;
        }

        return null;
    }
}
