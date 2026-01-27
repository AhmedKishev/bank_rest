package com.example.bankcards.exception;

import lombok.Builder;

import java.util.List;

@Builder
public class ErrorResponse {
    Throwable cause;
    List<StackTraceElement> stackTrace;
    String httpStatus;
    String userMessage;
    String message;
    List<Throwable> suppressed;
    String localizedMessage;
}
