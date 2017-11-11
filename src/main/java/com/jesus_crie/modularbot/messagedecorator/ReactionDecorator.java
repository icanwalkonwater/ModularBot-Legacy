package com.jesus_crie.modularbot.messagedecorator;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.config.DecoratorCache;
import com.jesus_crie.modularbot.exception.InvalidTimeoutException;
import com.jesus_crie.modularbot.listener.WaiterListener;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.exceptions.ErrorResponseException;
import net.dv8tion.jda.core.utils.Checks;

import java.io.IOException;
import java.util.HashMap;

/**
 * Base code for any message decorator.
 */
@JsonSerialize(using = ReactionDecorator.DecoratorSerializer.class)
public abstract class ReactionDecorator {

    protected Message bindTo;
    protected final User target;
    protected final long timeout;
    protected final HashMap<String, ReactionButton> buttons = new HashMap<>();
    @SuppressWarnings("unchecked")
    protected WaiterListener<? extends GenericMessageReactionEvent> listener = WaiterListener.EMPTY;
    protected DecoratorListener callback = null;

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
        if (!checkTimeout(timeout)) throw new InvalidTimeoutException("Invalid timeout: " + timeout + " !");

        bindTo = bind;
        this.target = target;
        this.timeout = timeout;
        for (ReactionButton button : buttons) {
            this.buttons.put(button.getEmoteString(), button);
            button.setupEmote(bind);
        }
        updateMessage();

        ModularBot.getDecoratorManager().registerDecorator(this);
        ModularBot.getDecoratorManager().getCache().tryCacheDecorator(this);
    }

    /**
     * Used to check if the timeout is valid.
     * @param timeout the timeout in millisecond.
     * @return true if everything is ok otherwise false and a {@link InvalidTimeoutException} will be raised.
     */
    protected boolean checkTimeout(long timeout) {
        return timeout >= 0 && isAlive;
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
     * Used for the cache.
     * Get the timestamp when the decorator will expire or 0 for infinite.
     * @return a timestamp.
     */
    public long getExpireTime() {
        if (timeout == 0 || !isAlive) return 0;
        return System.currentTimeMillis() + timeout;
    }

    /**
     * Used to check if the decorator is destroyed or not.
     * @return true if the decorator is active, otherwise false.
     */
    public boolean isAlive() {
        return isAlive;
    }

    /**
     * Attach a {@link DecoratorListener} to this decorator.
     * @param listener the listener to attach.
     */
    public void setListener(DecoratorListener listener) {
        callback = listener;
    }

    /**
     * Remove the attached {@link DecoratorListener} if one has been
     * set with {@link #setListener(DecoratorListener)}.
     */
    public void removeListener() {
        callback = null;
    }

    /**
     * Update the targeted message or end the decorator if the message has been deleted.
     * @return a updated instance of {@link Message}.
     */
    protected Message updateMessage() {
        if (!isAlive) {
            bindTo = null;
            return null;
        }

        try {
            bindTo = bindTo.getChannel().getMessageById(bindTo.getIdLong()).complete();
        } catch (ErrorResponseException e) {
            if (isAlive) destroy();
        }
        return bindTo;
    }

    /**
     * Triggered when a button is clicked.
     * Implementations must call this method when they create there {@link WaiterListener}.
     * @param event the event that has triggered the button.
     * @return the possibly-null button that has been clicked for further treatment.
     */
    protected ReactionButton click(MessageReactionAddEvent event) {
        if (!isAlive) return null;
        String identifier = event.getReactionEmote().isEmote() ? event.getReactionEmote().getId() : event.getReactionEmote().getName();

        ReactionButton button = buttons.getOrDefault(identifier, null);
        if (callback != null && callback.onClick(this, button)) return null;
        return button;
    }

    /**
     * Called when the decorator is destroyed, this happens when the bot is stopping,
     * when the message is deleted or when the message is dismissed.
     */
    public void destroy() {
        //updateMessage();
        isAlive = false;
        if (callback != null) callback.onDestroy(this);
        ModularBot.getDecoratorManager().unregister(this);
        /*if (bindTo != null) bindTo.getReactions().stream()
                .filter(MessageReaction::isSelf)
                .forEach(r -> r.removeReaction(bindTo.getJDA().getSelfUser()).complete());*/
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
            if (value instanceof Cacheable && value.isAlive) {
                gen.writeStartObject();
                gen.writeStringField("@class", value.getClass().getName());
                gen.writeNumberField("message", value.getMessage().getIdLong());
                gen.writeStringField("source", DecoratorCache.serializeSource(value.getMessage()));
                ((Cacheable) value).serialize(gen, provider);
                gen.writeEndObject();
            }
        }
    }
}
