package com.jesus_crie.modularbot.manager;

import com.jesus_crie.modularbot.command.Command;
import com.jesus_crie.modularbot.exception.*;
import com.jesus_crie.modularbot.listener.CommandEvent;
import com.jesus_crie.modularbot.listener.CommandHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandManager {

    private final List<Command> discordCommands = new ArrayList<>();
    private final CommandHandler handler;

    public CommandManager(CommandHandler handler) {
        this.handler = handler;
    }

    /**
     * Used to register some commands
     * @param commands
     */
    public void registerCommands(Command... commands) {
        discordCommands.addAll(Arrays.asList(commands));
    }

    public List<Command> getCommands() {
        return discordCommands;
    }

    public Command getCommand(final String alias) {
        return discordCommands.stream()
                .filter(c -> c.getAliases().contains(alias))
                .findFirst()
                .orElse(null);
    }

    public void handleCommand(CommandEvent event) throws WrongContextException, LowAccessLevelException, MissingPermissionException,
            CommandFailedException, NoPatternException {
        handler.onCommand(event);
    }

    public void handleCommandError(CommandException error) {
        if (error instanceof CommandNotFoundException)
            handler.onCommandNotFound((CommandNotFoundException) error, ((CommandNotFoundException) error).getNotFoundCommand());
        else if (error instanceof WrongContextException)
            handler.onCommandWrongContext((WrongContextException) error);
        else if (error instanceof LowAccessLevelException)
            handler.onCommandLowAccessLevel(((LowAccessLevelException) error));
        else if (error instanceof MissingPermissionException)
            handler.onCommandMissingPermission((MissingPermissionException) error);
        else if (error instanceof CommandFailedException)
            handler.onCommandFailed((CommandFailedException) error);
        else if (error instanceof NoPatternException)
            handler.onCommandNoPattern(((NoPatternException) error));
        else
            handler.onCommandErrorUnknown(error);
    }
}
