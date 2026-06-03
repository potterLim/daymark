package com.potterlim.daymark.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

final class AuthenticationViewSupport {

    private AuthenticationViewSupport() {
    }

    static boolean isAuthenticated(Authentication authenticationOrNull) {
        return authenticationOrNull != null
            && authenticationOrNull.isAuthenticated()
            && !(authenticationOrNull instanceof AnonymousAuthenticationToken);
    }
}
