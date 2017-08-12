package com.jesus_crie.modularbot.manager;

import com.jesus_crie.modularbot.command.ModularCommand;
import com.jesus_crie.modularbot.exception.*;
import com.jesus_crie.modularbot.listener.CommandHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandManager {

    private final List<ModularCommand> discordCommands = new ArrayList<>();
    private final CommandHandler handler;

    public CommandManager(CommandHandler handler) {
        this.handler = handler;
    }

    public void registerCommands(ModularCommand... commands) {
        discordCommands.addAll(Arrays.asList(commands));
    }

    public List<ModularCommand> getCommands() {
        return discordCommands;
    }

    public ModularCommand getCommand(final String alias) {
        return discordCommands.stream()
                .filter(c -> c.getAliases().contains(alias))
                .findFirst()
                .orElse(null);
    }

    public void handleCommandError(CommandException error) {
        if (error instanceof CommandNotFoundException)
            handler.onCommandNotFound((CommandNotFoundException) error, ((CommandNotFoundException) error).getNotFoundCommand());
        else if (error instanceof CommandWrongContextException)
            handler.onCommandWrongContext((CommandWrongContextException) error);
        else if (error instanceof CommandMissingPermissionException)
            handler.onCommandMissingPermission((CommandMissingPermissionException) error);
        else if (error instanceof CommandFailedException)
            handler.onCommandFailed((CommandFailedException) error);
        else
            handler.onCommandErrorUnknown(error);
    }
}
