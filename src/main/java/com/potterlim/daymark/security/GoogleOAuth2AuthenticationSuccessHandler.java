package com.potterlim.daymark.security;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import com.potterlim.daymark.dto.auth.GoogleRegistrationSession;
import com.potterlim.daymark.entity.EOperationEventType;
import com.potterlim.daymark.entity.UserAccount;
import com.potterlim.daymark.service.IUserAccountService;
import com.potterlim.daymark.service.OperationUsageEventService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class GoogleOAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final IUserAccountService mUserAccountService;
    private final OperationUsageEventService mOperationUsageEventService;
    private final ApplicationAuthenticationService mApplicationAuthenticationService;
    private final PostLoginRedirectPathResolver mPostLoginRedirectPathResolver;

    public GoogleOAuth2AuthenticationSuccessHandler(
        IUserAccountService userAccountService,
        OperationUsageEventService operationUsageEventService,
        ApplicationAuthenticationService applicationAuthenticationService,
        PostLoginRedirectPathResolver postLoginRedirectPathResolver
    ) {
        mUserAccountService = userAccountService;
        mOperationUsageEventService = operationUsageEventService;
        mApplicationAuthenticationService = applicationAuthenticationService;
        mPostLoginRedirectPathResolver = postLoginRedirectPathResolver;
    }

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse,
        Authentication authentication
    ) throws IOException, ServletException {
        GoogleIdentity googleIdentity = resolveGoogleIdentity(authentication);
        if (!googleIdentity.isUsable()) {
            mApplicationAuthenticationService.clearAuthentication(httpServletRequest, httpServletResponse);
            httpServletResponse.sendRedirect("/login?google=unverified");
            return;
        }

        Optional<UserAccount> userAccountOrEmpty =
            mUserAccountService.findUserAccountByGoogleSubject(googleIdentity.subject())
                .or(() -> mUserAccountService.connectGoogleIdentityByEmailAddress(
                    googleIdentity.emailAddress(),
                    googleIdentity.subject()
                ));
        if (userAccountOrEmpty.isPresent()) {
            mApplicationAuthenticationService.authenticateUserAccount(
                userAccountOrEmpty.get(),
                httpServletRequest,
                httpServletResponse
            );
            mOperationUsageEventService.recordUserEvent(
                EOperationEventType.SIGN_IN_SUCCEEDED,
                userAccountOrEmpty.get().getUserAccountId()
            );
            httpServletResponse.sendRedirect(mPostLoginRedirectPathResolver.resolveSavedRequestRedirectPath(
                httpServletRequest,
                httpServletResponse
            ));
            return;
        }

        mApplicationAuthenticationService.clearAuthentication(httpServletRequest, httpServletResponse);
        httpServletRequest.getSession(true).setAttribute(
            GoogleRegistrationSession.SESSION_ATTRIBUTE_NAME,
            new GoogleRegistrationSession(
                googleIdentity.subject(),
                googleIdentity.emailAddress(),
                googleIdentity.displayName()
            )
        );
        httpServletResponse.sendRedirect("/register");
    }

    private static GoogleIdentity resolveGoogleIdentity(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof OAuth2User oAuth2User)) {
            return GoogleIdentity.unusable();
        }

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String subject = readString(attributes, "sub");
        String emailAddress = readString(attributes, "email");
        String displayName = readString(attributes, "name");
        boolean isEmailVerified = Boolean.TRUE.equals(attributes.get("email_verified"))
            || "true".equalsIgnoreCase(String.valueOf(attributes.get("email_verified")));

        return new GoogleIdentity(subject, emailAddress, displayName, isEmailVerified);
    }

    private static String readString(Map<String, Object> attributes, String key) {
        Object valueOrNull = attributes.get(key);
        return valueOrNull == null ? "" : valueOrNull.toString().trim();
    }

    private record GoogleIdentity(
        String subject,
        String emailAddress,
        String displayName,
        boolean isEmailVerified
    ) {

        private static GoogleIdentity unusable() {
            return new GoogleIdentity("", "", "", false);
        }

        private boolean isUsable() {
            return isEmailVerified
                && subject != null
                && !subject.isBlank()
                && emailAddress != null
                && !emailAddress.isBlank();
        }
    }
}
