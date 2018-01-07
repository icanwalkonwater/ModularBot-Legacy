package com.jesus_crie.modularbot.listener;

import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.command.Command;
import com.jesus_crie.modularbot.exception.CommandNotFoundException;
import com.jesus_crie.modularbot.sharding.ModularShard;
import com.jesus_crie.modularbot.stats.Stats;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

/**
 * Listen for message event and determine if it's a command and handle it.
 */
public final class CommandListener extends ListenerAdapter {

    @Override
    public final void onMessageReceived(MessageReceivedEvent event) {
        // Check if is self message.
        if (event.getAuthor().equals(event.getJDA().getSelfUser()))
            return;

        final String prefix = ModularBot.getConfig().getPrefixForGuild(event.getGuild());

        // Check if message start with the command prefix.
        if (!event.getMessage().getContentRaw().startsWith(prefix))
            return;

        final String[] fullCommand = event.getMessage().getContentRaw().substring(prefix.length()).split(" ");
        final Command command = ModularBot.getCommandManager().getCommand(fullCommand[0]);

        if (command == null) {
            ModularBot.getCommandManager().handleCommandError(new CommandNotFoundException(event, fullCommand[0]));
            return;
        }

        // Stats
        Stats.incrementCommand(command.getName());

        // Create the event.
        final CommandEvent cEvent = new CommandEvent(command, event, fullCommand);

        // Pass the event to the command handler in another thread.
        ((ModularShard) event.getJDA()).getCommandPool().execute(() -> ModularBot.getCommandManager().handleCommand(cEvent));
    }
}
