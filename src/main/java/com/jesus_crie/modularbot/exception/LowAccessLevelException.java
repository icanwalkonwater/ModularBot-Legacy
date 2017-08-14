package com.jesus_crie.modularbot.exception;

import com.jesus_crie.modularbot.listener.CommandEvent;

public class LowAccessLevelException extends CommandException {

    public LowAccessLevelException(CommandEvent event) {
        super(event);
    }
}
