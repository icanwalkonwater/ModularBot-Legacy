package com.jesus_crie.modularbot.exception;

import com.jesus_crie.modularbot.command.Command;
import com.jesus_crie.modularbot.listener.CommandEvent;
import com.jesus_crie.modularbot.sharding.ModularShard;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * An exception that will be throw only by commands.
 */
public class CommandException extends RuntimeException {

    protected final Command command;
    protected final MessageReceivedEvent event;

    /**
     * Constructor when the {@link CommandEvent} has not been sent yet.
     */
    public CommandException(MessageReceivedEvent event) {
        command = null;
        this.event = event;
    }

    /**
     * Constructor when the event has been triggered.
     * @param event
     */
    public CommandException(CommandEvent event) {
        command = event.getCommand();
        this.event = event.getTriggerEvent();
    }

    protected CommandException(CommandEvent event, Throwable cause) {
        super(cause);
        command = event.getCommand();
        this.event = event.getTriggerEvent();
    }

    /**
     * Get the command that has triggered this.
     * @return a {@link Command}.
     */
    public Command getCommand() {
        return command;
    }

    /**
     * Get the event that has trigger the command.
     * @return a JDA event.
     */
    public MessageReceivedEvent getTriggerEvent() {
        return event;
    }

    /**
     * Get the shard from where the command has been executed.
     * @return the current shard.
     */
    public ModularShard getShard() {
        return (ModularShard) event.getJDA();
    }
}
