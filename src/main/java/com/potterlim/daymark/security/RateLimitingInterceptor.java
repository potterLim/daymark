package com.potterlim.daymark.security;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import com.potterlim.daymark.entity.UserAccount;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitingInterceptor implements HandlerInterceptor {

    private static final String RATE_LIMIT_RESPONSE_TEXT =
        "요청이 잠시 제한되었습니다. 잠시 후 다시 시도해주세요.";
    private static final Duration ONE_MINUTE = Duration.ofMinutes(1L);
    private static final Duration TEN_MINUTES = Duration.ofMinutes(10L);
    private static final Duration ONE_HOUR = Duration.ofHours(1L);
    private static final Duration ONE_DAY = Duration.ofDays(1L);
    private static final int GENERAL_VIEW_LIMIT_PER_MINUTE = 120;
    private static final int LOGIN_IP_LIMIT_PER_TEN_MINUTES = 10;
    private static final int LOGIN_IDENTIFIER_LIMIT_PER_TEN_MINUTES = 5;
    private static final int GOOGLE_LOGIN_START_LIMIT_PER_TEN_MINUTES = 20;
    private static final int WORKSPACE_CREATE_LIMIT_PER_HOUR = 5;
    private static final int RECORD_SAVE_LIMIT_PER_TEN_MINUTES = 30;
    private static final int EXPORT_LIMIT_PER_TEN_MINUTES = 10;
    private static final int EXPORT_LIMIT_PER_DAY = 50;

    private final InMemoryRateLimiter mInMemoryRateLimiter;

    public RateLimitingInterceptor(InMemoryRateLimiter inMemoryRateLimiter) {
        mInMemoryRateLimiter = inMemoryRateLimiter;
    }

    @Override
    public boolean preHandle(
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse,
        Object handler
    ) throws IOException {
        List<RateLimitCheck> rateLimitChecks = createRateLimitChecks(httpServletRequest);
        for (RateLimitCheck rateLimitCheck : rateLimitChecks) {
            boolean isAllowed = mInMemoryRateLimiter.tryAcquire(rateLimitCheck);
            if (!isAllowed) {
                writeRateLimitResponse(httpServletResponse);
                return false;
            }
        }

        return true;
    }

    private List<RateLimitCheck> createRateLimitChecks(HttpServletRequest httpServletRequest) {
        String method = httpServletRequest.getMethod();
        String requestPath = httpServletRequest.getRequestURI();
        String clientAddress = resolveClientAddress(httpServletRequest);
        List<RateLimitCheck> rateLimitChecks = new ArrayList<>();

        if ("GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method)) {
            rateLimitChecks.add(createRateLimitCheck(
                "general-view:" + clientAddress,
                GENERAL_VIEW_LIMIT_PER_MINUTE,
                ONE_MINUTE
            ));
        }

        if ("POST".equalsIgnoreCase(method) && "/login".equals(requestPath)) {
            rateLimitChecks.add(createRateLimitCheck(
                "login-ip:" + clientAddress,
                LOGIN_IP_LIMIT_PER_TEN_MINUTES,
                TEN_MINUTES
            ));
            createLoginIdentifierKeyOrNull(httpServletRequest)
                .ifPresent(loginIdentifierKey -> rateLimitChecks.add(createRateLimitCheck(
                    loginIdentifierKey,
                    LOGIN_IDENTIFIER_LIMIT_PER_TEN_MINUTES,
                    TEN_MINUTES
                )));
        }

        if ("GET".equalsIgnoreCase(method) && "/oauth2/authorization/google".equals(requestPath)) {
            rateLimitChecks.add(createRateLimitCheck(
                "google-login-start:" + clientAddress,
                GOOGLE_LOGIN_START_LIMIT_PER_TEN_MINUTES,
                TEN_MINUTES
            ));
        }

        if ("POST".equalsIgnoreCase(method) && "/register".equals(requestPath)) {
            rateLimitChecks.add(createRateLimitCheck(
                "workspace-create:" + clientAddress,
                WORKSPACE_CREATE_LIMIT_PER_HOUR,
                ONE_HOUR
            ));
        }

        if (isRecordSaveRequest(method, requestPath)) {
            rateLimitChecks.add(createRateLimitCheck(
                "record-save:" + resolveUserOrClientKey(clientAddress),
                RECORD_SAVE_LIMIT_PER_TEN_MINUTES,
                TEN_MINUTES
            ));
        }

        if (isExportRequest(method, requestPath)) {
            String userOrClientKey = resolveUserOrClientKey(clientAddress);
            rateLimitChecks.add(createRateLimitCheck(
                "export-short:" + userOrClientKey,
                EXPORT_LIMIT_PER_TEN_MINUTES,
                TEN_MINUTES
            ));
            rateLimitChecks.add(createRateLimitCheck(
                "export-daily:" + userOrClientKey,
                EXPORT_LIMIT_PER_DAY,
                ONE_DAY
            ));
        }

        return rateLimitChecks;
    }

    private static RateLimitCheck createRateLimitCheck(
        String keyValue,
        int requestLimit,
        Duration windowDuration
    ) {
        return RateLimitCheck.of(
            RateLimitKey.create(keyValue),
            RateLimitRequestLimit.of(requestLimit),
            windowDuration
        );
    }

    private static Optional<String> createLoginIdentifierKeyOrNull(HttpServletRequest httpServletRequest) {
        String loginIdentifierOrNull = httpServletRequest.getParameter("loginIdentifier");
        if (loginIdentifierOrNull == null || loginIdentifierOrNull.isBlank()) {
            return Optional.empty();
        }

        return Optional.of("login-identifier:" + loginIdentifierOrNull.trim().toLowerCase(Locale.ROOT));
    }

    private static boolean isRecordSaveRequest(String method, String requestPath) {
        return "POST".equalsIgnoreCase(method)
            && ("/daymark/morning/save".equals(requestPath) || "/daymark/evening/save".equals(requestPath));
    }

    private static boolean isExportRequest(String method, String requestPath) {
        return "GET".equalsIgnoreCase(method)
            && ("/daymark/library/export/markdown".equals(requestPath)
                || "/daymark/library/export/pdf".equals(requestPath));
    }

    private static String resolveUserOrClientKey(String clientAddress) {
        Authentication authenticationOrNull = SecurityContextHolder.getContext().getAuthentication();
        if (authenticationOrNull != null && authenticationOrNull.getPrincipal() instanceof UserAccount userAccount) {
            return "user-" + userAccount.getUserAccountId().getValue();
        }

        return "client-" + clientAddress;
    }

    private static String resolveClientAddress(HttpServletRequest httpServletRequest) {
        String remoteAddressOrNull = httpServletRequest.getRemoteAddr();
        if (remoteAddressOrNull == null || remoteAddressOrNull.isBlank()) {
            return "unknown-client";
        }

        return remoteAddressOrNull;
    }

    private static void writeRateLimitResponse(HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        httpServletResponse.setContentType("text/plain;charset=UTF-8");
        httpServletResponse.getWriter().write(RATE_LIMIT_RESPONSE_TEXT);
    }
}
