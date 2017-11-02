package com.jesus_crie.modularbot.messagedecorator;

import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.listener.WaiterListener;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.utils.Checks;

import java.util.HashMap;

/**
 * Base code for any message decorator.
 */
public abstract class ReactionDecorator {

    protected final Message bindTo;
    protected final User target;
    protected final HashMap<String, ReactionButton> buttons = new HashMap<>();
    @SuppressWarnings("unchecked")
    protected WaiterListener<MessageReactionAddEvent> listener = WaiterListener.EMPTY;

    protected boolean isAlive = true;

    /**
     * Create a new decorator with some buttons and automatically register it.
     * @param bind the message to bind to.
     * @param target the targeted user.
     * @param buttons the buttons to add to the decorator.
     */
    protected ReactionDecorator(Message bind, User target, ReactionButton... buttons) {
        Checks.notNull(bind, "message");

        bindTo = bind;
        this.target = target;
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
}
