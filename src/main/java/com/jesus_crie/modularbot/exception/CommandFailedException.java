package com.jesus_crie.modularbot.exception;

import com.jesus_crie.modularbot.listener.CommandEvent;

public class CommandFailedException extends CommandException {

    private final Throwable reason;

    public CommandFailedException(CommandEvent event, Throwable reason) {
        super(event, reason);
        this.reason = reason;
    }
}
