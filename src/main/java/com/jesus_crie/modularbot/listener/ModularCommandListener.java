package com.jesus_crie.modularbot.listener;

import com.jesus_crie.modularbot.exception.*;

public interface ModularCommandListener {

    /**
     * Triggered when a command is detected.
     * Used to check things before executing a command.
     * Can be overrided to remove or add checks.
     * @param event the event that has triggered the command.
     */
    void onCommand(CommandEvent event) throws WrongContextException, LowAccessLevelException, MissingPermissionException, CommandFailedException, NoPatternException;

    /**
     * Triggered after a command has been executed.
     * @param event the event that has triggered the command.
     */
    void onCommandSuccess(CommandEvent event);

    /**
     * Triggered when someone type a command that didn't exist.
     * @param e the error.
     * @param notFound the command that he type but don't exist.
     */
    void onCommandNotFound(CommandNotFoundException e, String notFound);

    /**
     * Triggered when a command is triggered outside of his
     * context.
     * @param e the error.
     */
    void onCommandWrongContext(WrongContextException e);

    /**
     * Triggered when someone triggered a command but don't have the required access level.
     * @param e the error.
     */
    void onCommandLowAccessLevel(LowAccessLevelException e);

    /**
     * Triggered when the command required more permissions.
     * @param e the error containing the missing permission.
     */
    void onCommandMissingPermission(MissingPermissionException e);

    /**
     * Triggered when a unexpected error occurred in the execution of the command.
     * @param e the error.
     */
    void onCommandFailed(CommandFailedException e);

    /**
     * Triggered when no registered pattern corresponds.
     * @param e the error.
     */
    void onCommandNoPattern(NoPatternException e);

    /**
     * Triggered by any other type of error that can occurred.
     * Should never be throw.
     * @param e the error.
     */
    void onCommandErrorUnknown(CommandException e);
}
