package com.jesus_crie.modularbot.messagedecorator;

import com.jesus_crie.modularbot.messagedecorator.dismissible.NotificationDecorator;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.Checks;

public abstract class ReactionDecoratorBuilder<T extends ReactionDecoratorBuilder, V extends ReactionDecorator> {

    /**
     * Initialize a new builder to create a {@link NotificationDecorator}.
     * @param bindTo the targeted message.
     * @param target the targeted user.
     * @return a new {@link com.jesus_crie.modularbot.messagedecorator.dismissible.NotificationDecorator.NotificationBuilder}.
     */
    public static NotificationDecorator.NotificationBuilder newNotification(Message bindTo, User target) {
        Checks.notNull(bindTo, "message");
        Checks.notNull(target, "target");
        return new NotificationDecorator.NotificationBuilder(bindTo, target);
    }

    // Builder stuff.

    protected final Message bind;

    protected ReactionDecoratorBuilder(Message bind) {
        Checks.notNull(bind, "message");
        this.bind = bind;
    }

    protected abstract T useTimeout(long timeout);
    protected abstract T targetUser(User target);

    public abstract V build();
}
