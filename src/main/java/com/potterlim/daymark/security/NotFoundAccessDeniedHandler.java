package com.potterlim.daymark.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

@Component
public class NotFoundAccessDeniedHandler implements AccessDeniedHandler {

    private static final String NOT_FOUND_TEMPLATE_NAME = "error/404";

    private final SpringTemplateEngine mSpringTemplateEngine;

    public NotFoundAccessDeniedHandler(SpringTemplateEngine springTemplateEngine) {
        mSpringTemplateEngine = springTemplateEngine;
    }

    @Override
    public void handle(
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse,
        AccessDeniedException accessDeniedException
    ) throws IOException {
        httpServletResponse.setStatus(HttpStatus.NOT_FOUND.value());
        httpServletResponse.setCharacterEncoding(StandardCharsets.UTF_8.name());
        httpServletResponse.setContentType(MediaType.TEXT_HTML_VALUE);

        JakartaServletWebApplication servletWebApplication =
            JakartaServletWebApplication.buildApplication(httpServletRequest.getServletContext());
        WebContext webContext = new WebContext(
            servletWebApplication.buildExchange(httpServletRequest, httpServletResponse),
            httpServletRequest.getLocale()
        );

        mSpringTemplateEngine.process(NOT_FOUND_TEMPLATE_NAME, webContext, httpServletResponse.getWriter());
    }
}
