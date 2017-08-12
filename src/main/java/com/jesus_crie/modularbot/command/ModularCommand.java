package com.jesus_crie.modularbot.command;

import com.jesus_crie.modularbot.exception.CommandFailedException;
import com.jesus_crie.modularbot.exception.CommandMissingPermissionException;
import com.jesus_crie.modularbot.exception.CommandWrongContextException;
import com.jesus_crie.modularbot.listener.CommandEvent;

import java.util.List;

public abstract class ModularCommand {

    public List<String> getAliases() {
        return null;
    }

    public void execute(CommandEvent event) throws CommandWrongContextException, CommandMissingPermissionException, CommandFailedException {

    }
}
