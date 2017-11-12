package com.jesus_crie.modularbot.command;

import com.jesus_crie.modularbot.exception.CommandFailedException;
import com.jesus_crie.modularbot.exception.MissingPermissionException;
import com.jesus_crie.modularbot.exception.NoPatternException;
import com.jesus_crie.modularbot.listener.CommandEvent;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.exceptions.PermissionException;

import java.util.*;

public abstract class Command {

    protected final List<String> aliases = new ArrayList<>();
    protected final EnumSet<ChannelType> allowedContext;
    protected final AccessLevel accessLevel;
    protected final List<CommandPattern> patterns = new ArrayList<>();
    protected String description;

    /**
     * Default constructor, used to store the main information.
     * @param name the name of the command. Aliases can be add later.
     * @param context the required context to execute the command. Use {@link Contexts}
     * @param level the access level required.
     */
    public Command(String name, EnumSet<ChannelType> context, AccessLevel level) {
        aliases.add(name);
        allowedContext = context;
        accessLevel = level;
    }

    /**
     * Used to register new patterns that can be used.
     * @param patterns the patterns to add.
     */
    protected void registerPatterns(CommandPattern... patterns) {
        this.patterns.addAll(Arrays.asList(patterns));
    }

    /**
     * Register only one pattern.
     * @param pattern the pattern to register.
     */
    protected void registerPattern(CommandPattern pattern) {
        patterns.add(pattern);
    }

    /**
     * Execute this command from the given event.
     * @param event the event.
     * @throws CommandFailedException if something unknown happen.
     * @throws MissingPermissionException if a permission is missing for an action.
     * @throws NoPatternException if no pattern is found and the command can't be executed.
     */
    public void execute(CommandEvent event) throws CommandFailedException, MissingPermissionException, NoPatternException {
        // Get the args without the command.
        final String[] args = Arrays.copyOfRange(event.getRawArgs(), 1, event.getRawArgs().length);
        try {
            // Iterate throw all patterns.
            for (CommandPattern pattern : patterns) {
                // If a pattern match.
                if (pattern.matchArgs(args)) {
                    // We run it.
                    pattern.run(event, args);
                    // And return.
                    return;
                }
            }
            // If the for ends, no pattern corresponds.
            throw new NoPatternException(event);
        } catch (PermissionException e) {
            throw new MissingPermissionException(event, e);
        } catch (NoPatternException e) {
            throw e;
        } catch (Exception e) {
            throw new CommandFailedException(event, e);
        }
    }

    /**
     * Get the name of the command.
     * @return the name of the command.
     */
    public String getName() {
        return aliases.get(0);
    }

    /**
     * Get the description of the command.
     * If it doesn't exist, return "No description".
     * @return the description of the command.
     */
    public String getDescription() {
        return description == null ? "No description." : description;
    }

    /**
     * Get all possible aliases of the command in a unmodifiable list.
     * @return a list of all aliases.
     */
    public List<String> getAliases() {
        return Collections.unmodifiableList(aliases);
    }

    /**
     * Get all command patterns.
     * Usefull for help command.
     * @return an unmodifiable list of all patterns.
     */
    public List<CommandPattern> getPatterns() {
        return Collections.unmodifiableList(patterns);
    }

    /**
     * Check if the current context is supported by the command.
     * @param type the current context.
     * @return true if the command can be executed from here, otherwise false.
     */
    public boolean checkContext(ChannelType type) {
        return allowedContext.contains(type);
    }

    /**
     * Get the required access level for this command.
     * @return the access level.
     */
    public AccessLevel getAccessLevel() {
        return accessLevel;
    }
}
