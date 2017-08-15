package com.jesus_crie.modularbot.exception;

import com.jesus_crie.modularbot.listener.CommandEvent;
import net.dv8tion.jda.core.entities.ChannelType;

public class WrongContextException extends CommandException {

    private final ChannelType rejected;

    public WrongContextException(CommandEvent event) {
        super(event);
        rejected = event.getTriggerEvent().getChannelType();
    }

    public ChannelType getRejectedContext() {
        return rejected;
    }
}
