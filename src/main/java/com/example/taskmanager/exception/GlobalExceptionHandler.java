package com.example.taskmanager.exception;

import com.example.taskmanager.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
/*
Combination of:

@ControllerAdvice   // intercepts exceptions from all Controllers.
@ResponseBody       // serialize the return as JSON, opposite of @RequestBody.
*/

public class GlobalExceptionHandler { // You can have multiple @ExceptionHandler annotations for as many methods treating exceptions as you need.

    @ExceptionHandler(ResourceNotFoundException.class) // Tells Spring to call "handleNotFound()" when it finds an exception of type defined in constructor.
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(ResourceNotFoundException ex) {
        return new ErrorResponse(404, ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleInvalidCredentials(InvalidCredentialsException ex) {
        return new ErrorResponse(401, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneric(Exception ex) {
        return new ErrorResponse(500, "An unexpected error occurred.");
    }
}
