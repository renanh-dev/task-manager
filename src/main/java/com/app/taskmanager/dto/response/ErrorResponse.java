package com.app.taskmanager.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int status,
        String message,
        List<FieldError> errors,
        Instant timeStamp
) {
    public record FieldError(String field, String message) {}

    public static ErrorResponse of(int status, String message) {
        return new ErrorResponse(status, message, null, Instant.now());
    }

    public static ErrorResponse ofErrors(int status, List<FieldError> errors) {
        return new ErrorResponse(status, null, errors, Instant.now());
    }
}
