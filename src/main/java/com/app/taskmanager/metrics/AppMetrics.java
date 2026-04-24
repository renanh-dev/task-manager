package com.app.taskmanager.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppMetrics {
    private final MeterRegistry registry;

    public void incrementRegistrations() {
        registry.counter("app.user.registrations").increment();
    }

    public void recordTaskCreation(String status) {
        registry.counter("app.task.created", "status", status).increment();
    }
}
