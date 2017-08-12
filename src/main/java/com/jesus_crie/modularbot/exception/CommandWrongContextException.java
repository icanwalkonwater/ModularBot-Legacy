package com.jesus_crie.modularbot.exception;

import com.jesus_crie.modularbot.listener.CommandEvent;
import net.dv8tion.jda.core.entities.ChannelType;

public class CommandWrongContextException extends CommandException {

    private final ChannelType rejected;

    public CommandWrongContextException(CommandEvent event) {
        super(event);
        rejected = event.getTriggerEvent().getChannelType();
    }

    public ChannelType getRejectedContext() {
        return rejected;
    }
}
