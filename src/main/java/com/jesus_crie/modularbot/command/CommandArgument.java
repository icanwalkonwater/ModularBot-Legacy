package com.jesus_crie.modularbot.command;

import com.jesus_crie.modularbot.sharding.ModularShard;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jesus_crie.modularbot.utils.F.f;

public class CommandArgument<T> implements Cloneable {

    /**
     * Pre-made argument that match a {@link Long}.
     * Can throw {@link NumberFormatException} because of the length
     * and the value of {@link Long#MAX_VALUE}.
     */
    public static final CommandArgument<Long> LONG = new CommandArgument<>("(?<value>-?[0-9]{1,19})",
            m -> Long.valueOf(m.group("value")), Long.class);

    /**
     * Pre-made argument that match an {@link Integer}.
     * As the same problem as {@link #LONG}.
     */
    public static final CommandArgument<Integer> INTEGER = new CommandArgument<>("(?<value>-?[0-9]{1,9})",
            m -> Integer.valueOf(m.group("value")), Integer.class);

    /**
     * Pre-made argument that match any Strings.
     */
    public static final CommandArgument<String> STRING = new CommandArgument<>("(?<value>[\\S]+)",
            m -> m.group("value"), String.class);

    /**
     * Pre-made argument that match only Strings with letters, numbers and underscores.
     */
    public static final CommandArgument<String> WORD_ONLY = new CommandArgument<>("(?<value>[\\w]+)",
            m -> m.group("value"), String.class);

    /**
     * Pre-made argument that return an URL as a String.
     */
    public static final CommandArgument<String> URL_AS_STRING = new CommandArgument<>("(?<url>(?:https?:\\/\\/){1}[a-z\\d.-]+(?:\\/[a-z\\d.-]*)*)",
            m -> m.group("url"), String.class);

    /**
     * Pre-made argument that return an instance of {@link URL}.
     * Can possibly break if my regex is wrong.
     */
    public static final CommandArgument<URL> URL = new CommandArgument<>("(?<url>(?:https?:\\/\\/){1}[a-z\\d.-]+(?:\\/[a-z\\d.-]*)*)",
            m -> {
                try {
                    return new URL(m.group("url"));
                } catch (MalformedURLException ignore) { return null; } //Will never happen if i'm good.
            }, java.net.URL.class);

    /**
     * Pre-made argument that match an email address.
     */
    public static final CommandArgument<String> MAIL = new CommandArgument<>("(?<mail>[a-z\\d.\\-\\+]+@[a-z\\d-.]+\\.[a-z]{2,6})",
            m -> m.group("mail"), String.class);

    /**
     * Pre-made argument that match an {@link User} by it's mention and using
     * his name and discriminator (Name#0001).
     * If the user doesn't exist on the current shard, return null.
     */
    public static final CommandArgument<User> USER = new CommandArgument<>("(?:<@!?(?<id>[0-9]*)>|(?<name>\\p{Graph}*)#(?<discriminator>[0-9]{4}))",
            (m, s) -> {
                if (m.group("id") != null && !m.group("id").isEmpty())
                    return s.getUserById(m.group("id"));
                else
                    return s.getUserByNameAndDiscriminator(m.group("name"), m.group("discriminator"));
            }, User.class);

    /**
     * Pre-made argument that match a {@link TextChannel} by it's mention.
     * If the channel doesn't exist on the current shard, return null.
     */
    public static final CommandArgument<TextChannel> CHANNEL = new CommandArgument<>("<#(?<id>[0-9]*)>",
            (m, s) -> s.getTextChannelById(m.group("id")), TextChannel.class);

    /**
     * Pre-made argument that match a {@link Role} by it's mention.
     * Works event if the role isn't mentionable by directly typing the mention with the role id.
     * If the role doesn't exist on the current shard, return null.
     */
    public static final CommandArgument<Role> ROLE = new CommandArgument<>("<@&(?<id>[0-9]*)>",
            (m, s) -> s.getRoleById(m.group("id")), Role.class);

    /**
     * Pre-made argument that match an {@link Emote} from a {@link net.dv8tion.jda.core.entities.Guild}.
     * WARNING, this don't match the general emotes because there are just plain unicode.
     * This only match the custom emotes of a Guild.
     */
    public static final CommandArgument<Emote> GUILD_EMOTE = new CommandArgument<>("<:[a-z_]*:(?<id>[0-9]*)>",
            (m, s) -> s.getEmoteById(m.group("id")), Emote.class);

    /**
     * Create a {@link CommandArgument} that match the exact provided String (case-insensitive).
     * Useful to create sub commands like "/config set ..."
     * @param name the exact string that you need to match.
     * @return an instance of {@link CommandArgument} that will match your string.
     */
    public static CommandArgument forString(String name) {
        return new CommandArgument<>(name, (m, s) -> name, String.class);
    }

    private final Class<T> clazz;
    private final Pattern pattern;
    private final BiFunction<Matcher, ModularShard, T> mapper;
    private boolean repeatable = false;

    /**
     * Default constructor, used every time.
     * @param pattern some regex that describe the required syntax.
     * @param mapper a lambda that take the current {@link ModularShard} and the {@link Matcher} to produce {@link T}.
     */
    public CommandArgument(String pattern, BiFunction<Matcher, ModularShard, T> mapper, Class<T> clazz) {
        this.pattern = Pattern.compile("^" + pattern + "$", Pattern.UNICODE_CHARACTER_CLASS + Pattern.CASE_INSENSITIVE);
        this.mapper = mapper;
        this.clazz = clazz;
    }

    /**
     * Overload of {@link CommandArgument#CommandArgument(String, BiFunction, Class)} for arguments
     * that don't require the shard.
     * @param pattern some regex that represent the syntax of the argument.
     * @param mapper a lambda that take a {@link Matcher} to produce {@link T}.
     */
    public CommandArgument(String pattern, Function<Matcher, T> mapper, Class<T> clazz) {
        this.pattern = Pattern.compile("^" + pattern + "$", Pattern.UNICODE_CHARACTER_CLASS + Pattern.CASE_INSENSITIVE);
        this.mapper = (m, s) -> mapper.apply(m);
        this.clazz = clazz;
    }

    /**
     * Clone the current argument and return another version with {@link #repeatable} set to True.
     * Usefull if you want to match a sentence.
     * @return a clone with repeatable set to True.
     */
    public CommandArgument getRepeatable() {
        CommandArgument arg = ((CommandArgument) clone());
        arg.repeatable = true;
        return arg;
    }

    /**
     * Check id this argument can be repeated.
     * @return {@link #repeatable}.
     */
    public boolean isRepeatable() {
        return repeatable;
    }

    /**
     * Math the given string with the stored {@link #pattern}.
     * @param toMatch the string to test.
     * @return true if there is a match.
     */
    boolean match(String toMatch) {
        return pattern.matcher(toMatch).matches();
    }

    /**
     * Used to map the given String to the real Object {@link T}.
     * {@link #match(String)} need to be called before.
     * @param shard the current {@link ModularShard}.
     * @param toMatch the argument that was provided.
     * @return an instance of {@link T} made by the matcher or null if there was no matches.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    T map(ModularShard shard, String toMatch) {
        final Matcher m = pattern.matcher(toMatch);
        m.find();
        return mapper.apply(m, shard);
    }

    /**
     * Used in {@link #getRepeatable()}.
     * @return a clone of this object.
     */
    @Override
    protected Object clone() {
        try {
            return super.clone();
        } catch (Exception ignore) {} //Can't happen
        return this;
    }

    @Override
    public String toString() {
        if (repeatable)
            return f("<%s> [%s...]", clazz.getSimpleName());
        else
            return f("<%s>", clazz.getSimpleName());
    }
}
