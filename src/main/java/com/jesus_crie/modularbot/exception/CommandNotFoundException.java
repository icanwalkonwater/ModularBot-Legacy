package com.jesus_crie.modularbot.exception;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandNotFoundException extends CommandException {

    protected final String notFound;

    public CommandNotFoundException(MessageReceivedEvent event, String notFound) {
        super(event);
        this.notFound = notFound;
    }

    public String getNotFoundCommand() {
        return notFound;
    }
}
