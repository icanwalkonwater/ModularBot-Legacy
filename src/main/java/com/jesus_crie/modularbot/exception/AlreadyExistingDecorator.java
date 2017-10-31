package com.jesus_crie.modularbot.exception;

public class AlreadyExistingDecorator extends RuntimeException {

    public AlreadyExistingDecorator() {
    }

    public AlreadyExistingDecorator(String message) {
        super(message);
    }

    public AlreadyExistingDecorator(String message, Throwable cause) {
        super(message, cause);
    }

    public AlreadyExistingDecorator(Throwable cause) {
        super(cause);
    }
}
