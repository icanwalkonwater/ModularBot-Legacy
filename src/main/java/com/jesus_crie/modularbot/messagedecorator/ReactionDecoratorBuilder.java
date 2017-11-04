package com.jesus_crie.modularbot.messagedecorator;

import com.jesus_crie.modularbot.messagedecorator.dismissible.DialogDecorator;
import com.jesus_crie.modularbot.messagedecorator.dismissible.NotificationDecorator;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

public abstract class ReactionDecoratorBuilder<T extends ReactionDecoratorBuilder> {

    /**
     * Initialize a new builder to create a {@link NotificationDecorator}.
     * @return a new {@link com.jesus_crie.modularbot.messagedecorator.dismissible.NotificationDecorator.NotificationBuilder}.
     */
    public static NotificationDecorator.NotificationBuilder newNotification() {
        return new NotificationDecorator.NotificationBuilder();
    }

    /**
     * Initialize a new builder to create a {@link DialogDecorator}.
     * @return a new {@link com.jesus_crie.modularbot.messagedecorator.dismissible.DialogDecorator.DialogBuilder}.
     */
    public static DialogDecorator.DialogBuilder newDialogBox() {
        return new DialogDecorator.DialogBuilder();
    }

    // Builder stuff. Used in internal.

    protected abstract T useTimeout(long timeout);

    public static abstract class DecoratorGlobalBuilder<T extends DecoratorGlobalBuilder, V extends ReactionDecorator> extends ReactionDecoratorBuilder<T> {

        public abstract V bindAndBuild(Message bind);
    }

    public static abstract class DecoratorTargetBuilder<T extends DecoratorTargetBuilder, V extends ReactionDecorator> extends ReactionDecoratorBuilder<T> {

        public abstract V bindAndBuild(Message bind, User target);
    }
}
