package com.jesus_crie.modularbot.messagedecorator.dismissible;

import com.jesus_crie.modularbot.messagedecorator.ReactionButton;
import com.jesus_crie.modularbot.messagedecorator.ReactionDecorator;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

/**
 * A decorator that can delete the attached message.
 * This decorator is specific for one user.
 */
public abstract class DismissibleDecorator extends ReactionDecorator {

    /**
     * Create a dismissible decorator with a button that will delete the message.
     * @param bind the message to bind to.
     * @param target the targeted user.
     * @param buttons the other buttons.
     */
    protected DismissibleDecorator(Message bind, User target, long timeout, ReactionButton... buttons) {
        super(bind, target, timeout, buttons);
    }

    /**
     * Used for the cache.
     * Get the timestamp when the decorator will expire.
     * @return a timestamp.
     */
    public long getExpireTime() {
        if (timeout == 0) return 0;
        return System.currentTimeMillis() + timeout;
    }

    /**
     * Delete the attached message and destroy the decorator.
     */
    protected final void dismiss() {
        destroy();
        bindTo.delete().complete();
    }
}
