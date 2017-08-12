package com.jesus_crie.modularbot.exception;

/**
 * An exception that will be throw only by commands.
 */
public class CommandException extends RuntimeException {

    public CommandException(String message) {
        super(message);
    }
}
