package com.jesus_crie.modularbot.command;

import com.jesus_crie.modularbot.listener.CommandEvent;
import com.jesus_crie.modularbot.sharding.ModularShard;
import com.jesus_crie.modularbot.utils.MiscUtils;
import net.dv8tion.jda.core.utils.Checks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class CommandPattern {

    private final List<CommandArgument> arguments;
    private final BiPredicate<CommandEvent, List<CommandArgument>> action;

    /**
     * Main constructor, create a pattern from an array of {@link CommandArgument} and
     * an action, generally a method reference.
     * @param args
     * @param action
     */
    public CommandPattern(CommandArgument[] args, BiPredicate<CommandEvent, List<CommandArgument>> action) {
        MiscUtils.notEmpty(args, "args");
        Checks.notNull(action, "action");

        arguments = Arrays.asList(args);
        this.action = action;
    }

    public CommandPattern(CommandArgument[] args, Predicate<CommandEvent> action) {
        this(args, (e, a) -> action.test(e));
    }

    public boolean hasArgument() {
        return !arguments.isEmpty();
    }

    /**
     * Used to check if the given args corresponds to the stored args.
     * @param args the args to check.
     * @return true if the given args matches the stored ones.
     */
    public boolean matchArgs(String[] args) {
        Checks.notNull(args, "args");

        if (!hasArgument())
            return args.length <= 0;

        if (args.length < arguments.size())
            return false;

        for (int i = 0; i < args.length; i++) {
            if (i >= arguments.size()) {
                if (arguments.get(arguments.size() - 1).isRepeatable()) {
                    if (!arguments.get(arguments.size() - 1).match(args[i]))
                        return false;
                    continue;
                } else
                    return false;
            }

            if (!arguments.get(i).match(args[i]))
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
        if (!hasArgument())
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
}
