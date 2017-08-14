package com.jesus_crie.modularbot.exception;

import com.jesus_crie.modularbot.listener.CommandEvent;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.exceptions.PermissionException;

public class MissingPermissionException extends CommandException {

    protected final Permission missing;

    public MissingPermissionException(CommandEvent event, PermissionException e) {
        super(event);
        missing = e.getPermission();
    }

    public Permission getMissingPermission() {
        return missing;
    }
}
