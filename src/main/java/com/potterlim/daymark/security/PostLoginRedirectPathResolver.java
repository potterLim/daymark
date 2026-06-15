package com.potterlim.daymark.security;

import java.net.URI;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

@Component
public class PostLoginRedirectPathResolver {

    private final RequestCache mRequestCache;

    public PostLoginRedirectPathResolver(RequestCache requestCache) {
        mRequestCache = requestCache;
    }

    public String resolveSavedRequestRedirectPath(
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse
    ) {
        if (httpServletRequest == null) {
            throw new IllegalArgumentException("httpServletRequest must not be null.");
        }

        if (httpServletResponse == null) {
            throw new IllegalArgumentException("httpServletResponse must not be null.");
        }

        SavedRequest savedRequestOrNull = mRequestCache.getRequest(httpServletRequest, httpServletResponse);
        if (savedRequestOrNull == null) {
            return "/";
        }

        mRequestCache.removeRequest(httpServletRequest, httpServletResponse);
        String redirectUrl = savedRequestOrNull.getRedirectUrl();
        if (redirectUrl == null || redirectUrl.isBlank()) {
            return "/";
        }

        try {
            URI redirectUri = URI.create(redirectUrl);
            String redirectPath = redirectUri.getRawPath();
            String redirectQueryOrNull = removeInternalSavedRequestQueryParameterOrNull(redirectUri.getRawQuery());
            if (!isAllowedPostLoginRedirectPath(redirectPath)) {
                return "/";
            }

            if (redirectQueryOrNull == null || redirectQueryOrNull.isBlank()) {
                return redirectPath;
            }

            return redirectPath + "?" + redirectQueryOrNull;
        } catch (IllegalArgumentException illegalArgumentException) {
            return "/";
        }
    }

    private static String removeInternalSavedRequestQueryParameterOrNull(String rawQueryOrNull) {
        if (rawQueryOrNull == null || rawQueryOrNull.isBlank()) {
            return null;
        }

        StringBuilder queryBuilder = new StringBuilder();
        for (String queryParameter : rawQueryOrNull.split("&")) {
            if (queryParameter.equals("continue") || queryParameter.equals("continue=")) {
                continue;
            }

            if (!queryBuilder.isEmpty()) {
                queryBuilder.append('&');
            }
            queryBuilder.append(queryParameter);
        }

        if (queryBuilder.isEmpty()) {
            return null;
        }

        return queryBuilder.toString();
    }

    private static boolean isAllowedPostLoginRedirectPath(String redirectPathOrNull) {
        return redirectPathOrNull != null
            && (redirectPathOrNull.equals("/account")
                || redirectPathOrNull.startsWith("/account/")
                || redirectPathOrNull.startsWith("/daymark/"));
    }
}
