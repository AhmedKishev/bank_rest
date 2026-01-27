package com.example.bankcards.exception;

public class AuthTokenNotValidException extends RuntimeException {
    public AuthTokenNotValidException(String message) {
        super(message);
    }
}
