package com.jesus_crie.modularbot.listener;

import com.jesus_crie.modularbot.command.ModularCommand;
import com.jesus_crie.modularbot.sharding.ModularShard;
import net.dv8tion.jda.core.events.message.GenericMessageEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandEvent extends GenericMessageEvent {

    private final ModularCommand command;
    private final MessageReceivedEvent triggerEvent;

    public CommandEvent(ModularCommand command, MessageReceivedEvent triggerEvent) {
        super(triggerEvent.getJDA(), triggerEvent.getResponseNumber(), triggerEvent.getMessageIdLong(), triggerEvent.getChannel());
        this.command = command;
        this.triggerEvent = triggerEvent;
    }

    /**
     * Return the current {@link ModularShard}.
     * @return the current {@link ModularShard}.
     */
    @Override
    public ModularShard getJDA() {
        return (ModularShard) api;
    }

    /**
     * Get the command that has been called.
     * @return a {@link ModularCommand}.
     */
    public ModularCommand getCommand() {
        return command;
    }

    /**
     * Get the event that has trigger the command.
     * @return a {@link MessageReceivedEvent}.
     */
    public MessageReceivedEvent getTriggerEvent() {
        return triggerEvent;
    }
}
