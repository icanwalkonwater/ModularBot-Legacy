package com.jesus_crie.modularbot.exception;

import com.jesus_crie.modularbot.listener.CommandEvent;

public class NoPatternException extends CommandException {

    public NoPatternException(CommandEvent event) {
        super(event);
    }
}
