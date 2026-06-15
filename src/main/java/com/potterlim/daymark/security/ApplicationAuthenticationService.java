package com.potterlim.daymark.security;

import com.potterlim.daymark.entity.UserAccount;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

@Component
public class ApplicationAuthenticationService {

    private final AuthenticationManager mAuthenticationManager;
    private final SecurityContextRepository mSecurityContextRepository;

    public ApplicationAuthenticationService(
        AuthenticationManager authenticationManager,
        SecurityContextRepository securityContextRepository
    ) {
        mAuthenticationManager = authenticationManager;
        mSecurityContextRepository = securityContextRepository;
    }

    /**
     * Authenticates a password login and persists it into the current Spring Security context.
     *
     * <p>Preconditions: the given login identifier must already exist and the raw password must be the
     * password entered by the user.</p>
     *
     * @return The authenticated principal saved into the current request context.
     */
    public Authentication authenticateWithPassword(
        String userName,
        String rawPassword,
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse
    ) {
        if (userName == null || userName.isBlank()) {
            throw new IllegalArgumentException("userName must not be blank.");
        }

        if (rawPassword == null) {
            throw new IllegalArgumentException("rawPassword must not be null.");
        }

        UsernamePasswordAuthenticationToken authenticationRequest =
            UsernamePasswordAuthenticationToken.unauthenticated(userName, rawPassword);
        Authentication authentication = mAuthenticationManager.authenticate(authenticationRequest);
        saveAuthentication(authentication, httpServletRequest, httpServletResponse);
        return authentication;
    }

    public Authentication authenticateUserAccount(
        UserAccount userAccount,
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse
    ) {
        if (userAccount == null) {
            throw new IllegalArgumentException("userAccount must not be null.");
        }

        Authentication authentication =
            new UsernamePasswordAuthenticationToken(userAccount, userAccount.getPassword(), userAccount.getAuthorities());
        saveAuthentication(authentication, httpServletRequest, httpServletResponse);
        return authentication;
    }

    public void clearAuthentication(
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse
    ) {
        validateRequestContext(httpServletRequest, httpServletResponse);

        saveSecurityContext(SecurityContextHolder.createEmptyContext(), httpServletRequest, httpServletResponse);
    }

    private void saveAuthentication(
        Authentication authentication,
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse
    ) {
        if (authentication == null) {
            throw new IllegalArgumentException("authentication must not be null.");
        }

        validateRequestContext(httpServletRequest, httpServletResponse);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        saveSecurityContext(securityContext, httpServletRequest, httpServletResponse);
    }

    private void saveSecurityContext(
        SecurityContext securityContext,
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse
    ) {
        SecurityContextHolder.setContext(securityContext);
        mSecurityContextRepository.saveContext(securityContext, httpServletRequest, httpServletResponse);
    }

    private static void validateRequestContext(
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse
    ) {
        if (httpServletRequest == null) {
            throw new IllegalArgumentException("httpServletRequest must not be null.");
        }

        if (httpServletResponse == null) {
            throw new IllegalArgumentException("httpServletResponse must not be null.");
        }
    }
}
