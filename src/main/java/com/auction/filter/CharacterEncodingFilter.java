package com.auction.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

@WebFilter(filterName = "CharacterEncodingFilter", urlPatterns = {"/*"})
public class CharacterEncodingFilter implements Filter {

    private static final Logger logger = Logger.getLogger(CharacterEncodingFilter.class.getName());
    private String encoding = "UTF-8";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("Initializing CharacterEncodingFilter");

        String encodingParam = filterConfig.getInitParameter("encoding");
        if (encodingParam != null && !encodingParam.trim().isEmpty()) {
            this.encoding = encodingParam;
        }

        logger.info("Character encoding set to: " + this.encoding);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Set request encoding
        if (request.getCharacterEncoding() == null) {
            request.setCharacterEncoding(encoding);
        }

        // Set response encoding and content type
        response.setCharacterEncoding(encoding);
        httpResponse.setHeader("Content-Type", "text/html; charset=" + encoding);

        // Add security headers
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        httpResponse.setHeader("X-Frame-Options", "DENY");
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");

        // Log request details for debugging
        logger.info(String.format("Processing request: %s %s from %s",
                httpRequest.getMethod(),
                httpRequest.getRequestURI(),
                httpRequest.getRemoteAddr()));

        // Continue with the filter chain
        chain.doFilter(request, response);

        // Log response status
        logger.info(String.format("Response status: %d for %s",
                httpResponse.getStatus(),
                httpRequest.getRequestURI()));
    }

    @Override
    public void destroy() {
        logger.info("Destroying CharacterEncodingFilter");
    }
}