package com.app.taskmanager.exception;

public class RefreshTokenException extends InvalidCredentialsException {
    public RefreshTokenException(String message) {
        super(message);
    }
}
