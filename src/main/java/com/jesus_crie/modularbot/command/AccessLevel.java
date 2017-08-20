package com.jesus_crie.modularbot.command;

import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.config.ConfigHandler;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.Checks;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class AccessLevel {

    /**
     * Only the user with the id stored in {@link ConfigHandler#getCreatorId()} can pass this.
     */
    public static final AccessLevel CREATOR = new AccessLevel(u -> ModularBot.getConfig().getCreatorId() == u.getIdLong());

    /**
     * Only the very owner of the current guild can pass this.
     * (the one that can bypass all permissions in guilds).
     */
    public static final AccessLevel SERVER_OWNER = new AccessLevel((u, g) -> CREATOR.check(u, g) || g.getOwner().equals(g.getMember(u)));

    /**
     * Only users with the permission {@link Permission#ADMINISTRATOR} can pass this or be the server owner.
     */
    public static final AccessLevel ADMINISTRATOR = new AccessLevel((u, g) -> SERVER_OWNER.check(u, g) || g.getMember(u).hasPermission(Permission.ADMINISTRATOR));

    /**
     * Only users with the permission {@link Permission#MESSAGE_MANAGE} can pass this or be an administrator or the server owner.
     */
    public static final AccessLevel MODERATOR = new AccessLevel((u, g) -> ADMINISTRATOR.check(u, g)
            || g.getMember(u).hasPermission(Permission.MESSAGE_MANAGE));

    /**
     * Only bot users can pass this.
     */
    public static final AccessLevel BOT = new AccessLevel(User::isBot);

    /**
     * Everyone is allowed.
     */
    public static final AccessLevel EVERYONE = new AccessLevel(u -> true);

    private final boolean useGuild;
    private final BiPredicate<User, Guild> checker;

    /**
     * Used for access levels that require to be in a guild.
     * @param checker the tester used to define if the tested user is allowed.
     */
    public AccessLevel(final BiPredicate<User, Guild> checker) {
        this.checker = checker;
        useGuild = true;
    }

    /**
     * Used for access levels that don't require to be in a guild.
     * @param checker the tester used to define if the tested user is allowed.
     */
    public AccessLevel(final Predicate<User> checker) {
        this.checker = (u, g) -> checker.test(u);
        useGuild = false;
    }

    /**
     * Used to check if a user can pass this level.
     * @param u the user to test. Must not be null.
     * @param g the guild to test with the user. Can be null.
     * @return true if the user is allowed, otherwise false.
     */
    @SuppressWarnings("SimplifiableConditionalExpression")
    public boolean check(User u, Guild g) {
        Checks.notNull(u, "user");
        return useGuild && g == null ? false : checker.test(u, g);
    }

    /**
     * @return true if the checkers are exactly the same (same instance).
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof AccessLevel && checker == ((AccessLevel) obj).checker;
    }
}
