package com.jesus_crie.modularbot.listener;

import com.jesus_crie.modularbot.exception.*;

public interface CommandHandler {

    default boolean onCommand(CommandEvent event) {
        return true;
    }

    void onCommandNotFound(CommandNotFoundException e, String notFound);

    void onCommandWrongContext(CommandWrongContextException e);

    void onCommandMissingPermission(CommandMissingPermissionException e);

    void onCommandFailed(CommandFailedException e);

    void onCommandErrorUnknown(CommandException e);
}
