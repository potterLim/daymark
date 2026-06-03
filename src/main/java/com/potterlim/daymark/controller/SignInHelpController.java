package com.potterlim.daymark.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SignInHelpController {

    @GetMapping("/forgot-password")
    public String redirectForgotPasswordPage() {
        return "redirect:/sign-in-help";
    }

    @GetMapping("/sign-in-help")
    public String showSignInHelpPage(Authentication authentication) {
        if (AuthenticationViewSupport.isAuthenticated(authentication)) {
            return "redirect:/account/password";
        }

        return "auth/sign-in-help";
    }
}
