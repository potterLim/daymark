package com.potterlim.daymark.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ProductErrorController implements ErrorController {

    private static final String NOT_FOUND_VIEW_NAME = "error/404";

    @RequestMapping("/error")
    public String showErrorPage(HttpServletResponse httpServletResponse) {
        return showNotFoundPage(httpServletResponse);
    }

    @RequestMapping("/error/not-found")
    public String showNotFoundPage(HttpServletResponse httpServletResponse) {
        httpServletResponse.setStatus(HttpStatus.NOT_FOUND.value());
        return NOT_FOUND_VIEW_NAME;
    }
}
