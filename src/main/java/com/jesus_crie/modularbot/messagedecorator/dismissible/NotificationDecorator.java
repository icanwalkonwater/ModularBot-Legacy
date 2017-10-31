package com.jesus_crie.modularbot.messagedecorator.dismissible;

import com.jesus_crie.modularbot.messagedecorator.ReactionDecoratorBuilder;
import com.jesus_crie.modularbot.sharding.ModularShard;
import com.jesus_crie.modularbot.utils.Waiter;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.utils.Checks;

public class NotificationDecorator extends DismissibleDecorator {

    /**
     * The unicode string for "❌".
     */
    public static final String RED_CROSS = "\u274C";


    /**
     * Main constructor.
     * See {@link NotificationBuilder} for more details.
     */
    private NotificationDecorator(Message bind, User target, long timeout) {
        super(bind, target, RED_CROSS);

        listener = Waiter.createListener(((ModularShard) bind.getJDA()),
                MessageReactionAddEvent.class,
                e -> e.getMessageIdLong() == bind.getIdLong() && e.getUser().equals(target),
                this::onClick, this::onDestroy,
                timeout, true);
    }

    /**
     * Builder for notifications.
     */
    public static final class NotificationBuilder extends ReactionDecoratorBuilder<NotificationBuilder, NotificationDecorator> {

        private long timeout = 0;
        private final User target;

        /**
         * Main constructor of this builder.
         * @param bind the message to bind to.
         * @param target the targeted user.
         */
        public NotificationBuilder(Message bind, User target) {
            super(bind);
            Checks.notNull(target, "target");
            this.target = target;
        }

        /**
         * Not used here.
         */
        @Override
        protected final NotificationBuilder targetUser(User target) { return this; }

        /**
         * Used to add a timeout to the notification.
         * When the timeout is reached, the notification is no longer interactive.
         * @param timeout the timeout in milliseconds.
         * @return the current builder.
         */
        @Override
        public NotificationBuilder useTimeout(long timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * Create a {@link NotificationDecorator} based on the current builder.
         * @return a new {@link NotificationDecorator}.
         */
        @Override
        public NotificationDecorator build() {
            return new NotificationDecorator(bind, target, timeout);
        }
    }
}
