package com.example.taskmanager.dto.response;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public record ErrorResponse(
        int status,
        String message,
        ZonedDateTime timeStamp
) {
    public ErrorResponse(int status, String message) {
        this(status, message, ZonedDateTime.now(ZoneId.of("UTC")));
    }
}
