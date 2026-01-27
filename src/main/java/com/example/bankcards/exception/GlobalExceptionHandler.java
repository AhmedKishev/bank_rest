package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse notFoundException(final RuntimeException e) {
        return ErrorResponse.builder()
                .cause(e.getCause())
                .stackTrace(Arrays.asList(e.getStackTrace()))
                .httpStatus(HttpStatus.BAD_REQUEST.name())
                .userMessage(e.getMessage())
                .message("Bad request")
                .suppressed(Arrays.asList(e.getSuppressed()))
                .localizedMessage(e.getLocalizedMessage())
                .build();
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse usernameAlreadyExistsException(final RuntimeException e) {
        return ErrorResponse.builder()
                .cause(e.getCause())
                .stackTrace(Arrays.asList(e.getStackTrace()))
                .httpStatus(HttpStatus.CONFLICT.name())
                .userMessage(e.getMessage())
                .message("Conflict")
                .suppressed(Arrays.asList(e.getSuppressed()))
                .localizedMessage(e.getLocalizedMessage())
                .build();
    }


    @ExceptionHandler(AuthTokenNotValidException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse authTokenNotValidException (final RuntimeException e) {
        return ErrorResponse.builder()
                .cause(e.getCause())
                .stackTrace(Arrays.asList(e.getStackTrace()))
                .httpStatus(HttpStatus.CONFLICT.name())
                .userMessage(e.getMessage())
                .message("Unauthorized")
                .suppressed(Arrays.asList(e.getSuppressed()))
                .localizedMessage(e.getLocalizedMessage())
                .build();
    }


}
