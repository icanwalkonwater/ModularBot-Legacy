package com.jesus_crie.modularbot.messagedecorator.dismissible;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.jesus_crie.modularbot.messagedecorator.Cacheable;
import com.jesus_crie.modularbot.messagedecorator.ReactionButton;
import com.jesus_crie.modularbot.messagedecorator.ReactionDecoratorBuilder;
import com.jesus_crie.modularbot.sharding.ModularShard;
import com.jesus_crie.modularbot.utils.Waiter;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.utils.Checks;

import java.io.IOException;

public class NotificationDecorator extends DismissibleDecorator implements Cacheable {

    /**
     * The {@link ReactionButton} used to dismiss a notification.
     * The emote used is "❌"
     */
    protected static final ReactionButton DISMISS_BUTTON = new ReactionButton("\u274C", (event, decorator) -> ((DismissibleDecorator) decorator).dismiss());

    /**
     * Main constructor.
     * See {@link NotificationBuilder} for more details.
     */
    protected NotificationDecorator(Message bind, User target, long timeout) {
        super(bind, target, timeout, DISMISS_BUTTON);

        listener = Waiter.createListener(((ModularShard) bind.getJDA()),
                MessageReactionAddEvent.class,
                e -> isAlive && e.getMessageIdLong() == bind.getIdLong() && e.getUser().equals(target),
                this::click, this::destroy,
                timeout, true);
    }

    // Serialization stuff

    /**
     * Deserializer.
     * @param message the source message.
     * @param node the content node.
     */
    protected NotificationDecorator(Message message, JsonNode node) {
        this(message,
                message.getJDA().getUserById(node.get("user_target").asLong()),
                node.get("expire_at").asLong() - System.currentTimeMillis());
    }

    @Override
    public void serialize(JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeNumberField("user_target", target.getIdLong());
        gen.writeNumberField("expire_at", getExpireTime());
    }

    /**
     * The builder for this decorator
     */
    public static final class NotificationBuilder extends ReactionDecoratorBuilder.DecoratorTargetBuilder<NotificationBuilder, NotificationDecorator> {

        private long timeout = 0;

        /**
         * Used to add a timeout to the notification.
         * When the timeout is reached, the notification is no longer interactive.
         * By default, the timeout is infinite (0).
         * @param timeout the timeout in milliseconds.
         * @return the current builder.
         */
        @Override
        public NotificationBuilder useTimeout(long timeout) {
            if (timeout == -1) return this;
            else if (timeout <= 0) this.timeout = 0;
            else this.timeout = timeout;
            return this;
        }

        /**
         * Create a {@link NotificationDecorator} based on the current builder.
         * @return a new {@link NotificationDecorator}.
         */
        @Override
        public NotificationDecorator bindAndBuild(Message bind, User target) {
            Checks.notNull(bind, "message");
            Checks.notNull(target, "target");
            return new NotificationDecorator(bind, target, timeout);
        }
    }
}
