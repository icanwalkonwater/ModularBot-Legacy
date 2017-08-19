package com.jesus_crie.modularbot.listener;

import com.jesus_crie.modularbot.command.Command;
import com.jesus_crie.modularbot.sharding.ModularShard;
import com.jesus_crie.modularbot.utils.MiscUtils;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import static com.jesus_crie.modularbot.utils.F.f;

public class CommandEvent extends MessageReceivedEvent {

    private final Command command;
    private final MessageReceivedEvent triggerEvent;
    private final String[] rawArgs;

    public CommandEvent(Command command, MessageReceivedEvent triggerEvent, String[] rawArgs) {
        super(triggerEvent.getJDA(), triggerEvent.getResponseNumber(), triggerEvent.getMessage());
        this.command = command;
        this.triggerEvent = triggerEvent;
        this.rawArgs = rawArgs;
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
     * @return a {@link Command}.
     */
    public Command getCommand() {
        return command;
    }

    /**
     * Get the event that has trigger the command.
     * @return a {@link MessageReceivedEvent}.
     */
    public MessageReceivedEvent getTriggerEvent() {
        return triggerEvent;
    }

    /**
     * Get the not parsed arguments.
     * @return an arrays of the full command.
     */
    public String[] getRawArgs() {
        return rawArgs;
    }

    /**
     * Easily reply with a simple message.
     * Mainly for debug or quick commands.
     * @param message the message to send.
     * @return the newly created message.
     */
    public Message fastReply(String message) {
        return getChannel().sendMessage(message).complete();
    }

    /**
     * Used to print the event.
     * @return a string representing the event.
     */
    @Override
    public String toString() {
        if (triggerEvent.getGuild() != null)
            return f("%s from \"%s\" executed \"%s\"", MiscUtils.stringifyUser(triggerEvent.getAuthor()), triggerEvent.getGuild().getName(), triggerEvent.getMessage().getRawContent());
        else
            return f("%s executed \"%s\"", MiscUtils.stringifyUser(triggerEvent.getAuthor()), triggerEvent.getMessage().getRawContent());
    }
}
