package com.app.taskmanager.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        try {
            String requestId = UUID.randomUUID().toString().substring(0, 8);
            MDC.put("requestId", requestId);
            MDC.put("method", request.getMethod());
            MDC.put("path", request.getRequestURI());
            MDC.put("ip", request.getRemoteAddr());

            // if authenticated, pull the user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                MDC.put("username", auth.getName());
            }

            response.setHeader("X-Request-ID", requestId);
            chain.doFilter(request, response);
        } finally {
            MDC.clear(); // critical — threads are reused, stale context leaks otherwise
        }
    }
}

// logs include all the above information without any altering of the service code
