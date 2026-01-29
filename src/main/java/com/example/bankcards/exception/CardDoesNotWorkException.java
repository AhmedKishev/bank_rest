package com.example.bankcards.exception;

public class CardDoesNotWorkException extends RuntimeException {
    public CardDoesNotWorkException(String message) {
        super(message);
    }
}
