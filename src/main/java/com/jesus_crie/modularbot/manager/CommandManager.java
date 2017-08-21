package com.jesus_crie.modularbot.manager;

import com.jesus_crie.modularbot.command.AccessLevel;
import com.jesus_crie.modularbot.command.Command;
import com.jesus_crie.modularbot.command.QuickCommand;
import com.jesus_crie.modularbot.exception.*;
import com.jesus_crie.modularbot.listener.CommandEvent;
import com.jesus_crie.modularbot.listener.ModularCommandListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class CommandManager {

    private final List<Command> discordCommands = new ArrayList<>();
    private final ModularCommandListener handler;

    public CommandManager(ModularCommandListener handler) {
        this.handler = handler;
    }

    /**
     * Used to register some commands.
     * @param commands the commands to register.
     */
    public void registerCommands(Command... commands) {
        discordCommands.addAll(Arrays.asList(commands));
    }

    /**
     * Get an unmodifiable list of all registered commands.
     * @return a list of commands.
     */
    public List<Command> getCommands() {
        return Collections.unmodifiableList(discordCommands);
    }

    /**
     * Register a quick command with no argument accessible from everywhere by everybody.
     * @param name the name of the command.
     * @param action the action to perform.
     */
    public void registerQuickCommand(String name, Consumer<CommandEvent> action) {
        registerCommands(new QuickCommand(name, AccessLevel.EVERYONE, action));
    }

    /**
     * Register a quick command with no argument accessible from everywhere but only by admins.
     * @param name the name of the command.
     * @param action the action to perform.
     */
    public void registerAdminQuickCommand(String name, Consumer<CommandEvent> action) {
        registerCommands(new QuickCommand(name, AccessLevel.ADMINISTRATOR, action));
    }

    /**
     * Get a command by it's alias.
     * @param alias the alias.
     * @return the corresponding command.
     */
    public Command getCommand(final String alias) {
        return discordCommands.stream()
                .filter(c -> c.getAliases().contains(alias))
                .findFirst()
                .orElse(null);
    }

    /**
     * Used to handle the incoming command.
     * @param event the command event.
     */
    public void handleCommand(CommandEvent event) {
        try {
            handler.onCommand(event);
            handler.onCommandSuccess(event);
        } catch (CommandException e) {
            handleCommandError(e);
        }
    }

    /**
     * Used to handle an error that occurred before or during the execution of a command.
     * @param error the error.
     */
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
