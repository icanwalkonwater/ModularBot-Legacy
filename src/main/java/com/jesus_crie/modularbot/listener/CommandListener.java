package com.jesus_crie.modularbot.listener;

import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.command.ModularCommand;
import com.jesus_crie.modularbot.exception.CommandException;
import com.jesus_crie.modularbot.exception.CommandNotFoundException;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class CommandListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // Check if message start with the command prefix.
        if (!event.getMessage().getRawContent().startsWith(ModularBot.getConfig().getPrefixForGuild(event.getGuild())))
            return;

        // Check if is self message.
        if (event.getAuthor().equals(event.getJDA().getSelfUser()))
            return;

        final String[] fullCommand = event.getMessage().getRawContent().substring(ModularBot.getConfig().getPrefixForGuild(event.getGuild()).length()).split(" ");
        final ModularCommand command = ModularBot.getCommandManager().getCommand(fullCommand[0]);

        try {
            if (command == null)
                throw new CommandNotFoundException(event, fullCommand[0]);
            final CommandEvent cEvent = new CommandEvent(command, event);

        } catch (CommandException e) {
            ModularBot.getCommandManager().handleCommandError(e);
        }
    }
}
