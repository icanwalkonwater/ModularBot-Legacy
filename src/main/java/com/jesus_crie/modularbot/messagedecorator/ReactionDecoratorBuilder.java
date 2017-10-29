package com.jesus_crie.modularbot.messagedecorator;

import com.jesus_crie.modularbot.messagedecorator.dismissable.NotificationDecorator;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.Checks;

public abstract class ReactionDecoratorBuilder<T extends ReactionDecoratorBuilder, V extends ReactionDecorator> {

    public static NotificationDecorator.NotificationBuilder newNotification(Message bindTo, User target) {
        Checks.notNull(bindTo, "message");
        Checks.notNull(target, "target");
        return new NotificationDecorator.NotificationBuilder(bindTo, target);
    }

    protected final Message bind;

    protected ReactionDecoratorBuilder(Message bind) {
        Checks.notNull(bind, "message");
        this.bind = bind;
    }

    protected abstract T useTimeout(long timeout);
    protected abstract T targetUser(User target);

    public abstract V build();
}
