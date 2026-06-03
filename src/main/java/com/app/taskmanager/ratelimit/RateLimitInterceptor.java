package com.app.taskmanager.ratelimit;

import com.app.taskmanager.entity.User;
import com.app.taskmanager.exception.RateLimitException;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimiterService;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        String ip = resolveClientIp(request);
        String path = request.getRequestURI();

        Bucket bucket;

        if (path.equals("/api/auth/refresh")) {
            bucket = rateLimiterService.resolveBucket("refresh:" + ip, 3, Duration.ofMinutes(15));
        } else if (isAuthenticated()) {
            var principal = (User) SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getPrincipal();
            bucket = rateLimiterService.resolveBucket("user:" + principal.getId(), 20, Duration.ofMinutes(1));
        } else {
            bucket = rateLimiterService.resolveBucket("unauthenticated:" + ip, 5, Duration.ofMinutes(1));
        }

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.setHeader("X-Rate-Limit-Remaining",
                    String.valueOf(probe.getRemainingTokens()));
            return true;
        }

        long retryAfterSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
        throw new RateLimitException(retryAfterSeconds);
    }

    private String resolveClientIp(HttpServletRequest request) { // implementation designed for requests arriving from a trusted proxy
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private boolean isAuthenticated() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }
}