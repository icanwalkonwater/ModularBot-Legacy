package com.jesus_crie.modularbot.command;

import com.jesus_crie.modularbot.listener.CommandEvent;
import com.jesus_crie.modularbot.sharding.ModularShard;
import net.dv8tion.jda.core.utils.Checks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CommandPattern {

    private final List<Argument> arguments;
    private final BiConsumer<CommandEvent, List<Object>> action;

    /**
     * Main constructor, create a pattern from an array of {@link Argument} and
     * an action, generally a method reference.
     * @param args the arguments required to trigger the command.
     * @param action some code that will be executed if the pattern is triggered.
     */
    public CommandPattern(Argument[] args, BiConsumer<CommandEvent, List<Object>> action) {
        Checks.notNull(action, "action");

        if (args != null)
            arguments = Arrays.asList(args);
        else
            arguments = Collections.emptyList();
        this.action = action;
    }

    /**
     * Overload of {@link #CommandPattern(Argument[], BiConsumer)} for command that don't need the arguments.
     */
    public CommandPattern(Argument[] args, Consumer<CommandEvent> action) {
        this(args, (e, a) -> action.accept(e));
    }

    public boolean hasArguments() {
        return !arguments.isEmpty();
    }

    /**
     * Get a copy of all of the arguments of the pattern.
     * @return a unmodifiable copy of the arguments.
     */
    public List<Argument> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    /**
     * Used to check if the given args corresponds to the stored args.
     * @param args the args to check.
     * @return true if the given args matches the stored ones.
     */
    public boolean matchArgs(String[] args) {
        Checks.notNull(args, "args");

        if (!hasArguments())
            return args.length <= 0;

        if (args.length < arguments.size())
            return false;

        for (int i = 0; i < args.length; i++) {
            if (i >= arguments.size()) {
                if (arguments.get(arguments.size() - 1).isRepeatable()) {
                    if (arguments.get(arguments.size() - 1).noMatch(args[i]))
                        return false;
                    continue;
                } else
                    return false;
            }

            if (arguments.get(i).noMatch(args[i]))
                return false;
        }

        return true;
    }

    /**
     * Get and map all given args to a {@link List} of Objects.
     * {@link #matchArgs(String[])} need to be called first.
     * @param shard the current shard.
     * @param args the args to map.
     * @return a list of objects.
     */
    private List<Object> collectArgs(ModularShard shard, String[] args) {
        if (!hasArguments())
            return null;

        List<Object> out = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            if (i >= arguments.size()) {
                if (arguments.get(arguments.size() - 1).isRepeatable()) {
                    out.add(arguments.get(arguments.size() - 1).map(shard, args[i]));
                    continue;
                } else
                    return out;
            }

            out.add(arguments.get(i).map(shard, args[i]));
        }

        return out;
    }

    /**
     * Run the action of the pattern.
     * This assume that {@link #matchArgs(String[])} has returned true.
     * @param event the event containing information about the command.
     * @param args the arguments mapped from the patterns. Can be cast to the desired objects.
     */
    public void run(CommandEvent event, String[] args) {
        action.accept(event, collectArgs((ModularShard) event.getTriggerEvent().getJDA(), args));
    }
}
