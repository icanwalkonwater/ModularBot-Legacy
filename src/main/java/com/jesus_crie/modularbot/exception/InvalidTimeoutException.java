package com.jesus_crie.modularbot.exception;

public class InvalidTimeoutException extends RuntimeException {

    public InvalidTimeoutException(String message) {
        super(message);
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
