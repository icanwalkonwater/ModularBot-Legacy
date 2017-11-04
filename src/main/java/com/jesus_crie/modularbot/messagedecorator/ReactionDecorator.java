package com.jesus_crie.modularbot.messagedecorator;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.exception.InvalidTimeoutException;
import com.jesus_crie.modularbot.listener.WaiterListener;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.utils.Checks;

import java.io.IOException;
import java.util.HashMap;

/**
 * Base code for any message decorator.
 */
@JsonSerialize(using = ReactionDecorator.DecoratorSerializer.class)
public abstract class ReactionDecorator {

    protected final Message bindTo;
    protected final User target;
    protected final long timeout;
    protected final HashMap<String, ReactionButton> buttons = new HashMap<>();
    @SuppressWarnings("unchecked")
    protected WaiterListener<MessageReactionAddEvent> listener = WaiterListener.EMPTY;

    protected boolean isAlive = true;

    /**
     * Create a new decorator with some buttons and automatically register it.
     * @param bind the message to bind to.
     * @param target the targeted user.
     * @param buttons the buttons to add to the decorator.
     * @throws InvalidTimeoutException when the timeout is < 0.
     */
    protected ReactionDecorator(Message bind, User target, long timeout, ReactionButton... buttons) {
        Checks.notNull(bind, "message");
        if (timeout < 0) throw new InvalidTimeoutException("Invalid timeout: " + timeout + " !");

        bindTo = bind;
        this.target = target;
        this.timeout = timeout;
        for (ReactionButton button : buttons) {
            this.buttons.put(button.getEmoteString(), button);
            button.setupEmote(bind);
        }

        ModularBot.getDecoratorManager().registerDecorator(this);
        ModularBot.getDecoratorManager().getCache().tryCacheDecorator(this);
    }

    /**
     * Get the target message.
     * @return the targeted message.
     */
    public Message getMessage() {
        return bindTo;
    }

    /**
     * Get the targeted message if one is defined.
     * @return a possibly-null {@link User}.
     */
    public User getTarget() {
        return target;
    }

    /**
     * Used to check if the decorator is destroyed or not.
     * @return true if the decorator is active, otherwise false.
     */
    public boolean isAlive() {
        return isAlive;
    }

    /**
     * Triggered when a button is clicked.
     * Implementations must call this method when they create there {@link WaiterListener}.
     * @param event the event that has triggered the button.
     */
    protected void onClick(MessageReactionAddEvent event) {
        String identifier = event.getReactionEmote().isEmote() ? event.getReactionEmote().getId() : event.getReactionEmote().getName();
        ReactionButton button = buttons.getOrDefault(identifier, null);
        if (button != null) button.onClick(event, this);
    }

    /**
     * Called when the decorator is destroyed, this happens when the bot is stopping,
     * when the message is deleted or when the message is dismissed.
     */
    public void destroy() {
        isAlive = false;
        ModularBot.getDecoratorManager().unregister(this);
        bindTo.clearReactions().complete();
        listener.cancel(true);
    }

    /**
     * Check if 2 decorators are equals.
     * @param obj the other objet to compare to.
     * @return true if the 2 objects are equals.
     */
    @Override
    public boolean equals(Object obj) {
        return getClass().getName().equals(obj.getClass().getName())
                && bindTo.equals(((ReactionDecorator) obj).bindTo)
                && buttons.size() == ((ReactionDecorator) obj).buttons.size();
    }

    public static final class DecoratorSerializer extends StdSerializer<ReactionDecorator> {

        protected DecoratorSerializer() {
            super(ReactionDecorator.class);
        }

        @Override
        public void serialize(ReactionDecorator value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            if (value instanceof Cacheable) {
                ((Cacheable) value).serialize(gen, provider);
            }
        }
    }
}
