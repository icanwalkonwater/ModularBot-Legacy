package com.jesus_crie.modularbot.command;

import com.jesus_crie.modularbot.listener.CommandEvent;

import java.util.function.Consumer;

public class QuickCommand extends Command {

    public QuickCommand(String name, AccessLevel level, Consumer<CommandEvent> action) {
        super(name, Contexts.EVERYWHERE, level);
        registerPattern(new CommandPattern(null, action));
    }
}
