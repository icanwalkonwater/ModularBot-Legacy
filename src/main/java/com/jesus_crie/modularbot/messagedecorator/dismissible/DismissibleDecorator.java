package com.jesus_crie.modularbot.messagedecorator.dismissible;

import com.jesus_crie.modularbot.messagedecorator.ReactionButton;
import com.jesus_crie.modularbot.messagedecorator.ReactionDecorator;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

/**
 * A decorator that can delete the attached message.
 */
public abstract class DismissibleDecorator extends ReactionDecorator {

    /**
     * Create a dismissible decorator with a button that will delete the message.
     * @param bind the message to bind to.
     * @param target the targeted user.
     * @param buttons the other buttons.
     */
    protected DismissibleDecorator(Message bind, User target, ReactionButton... buttons) {
        super(bind, target, buttons);
    }

    @Override
    public final void onDestroy() {
        super.onDestroy();
        listener.cancel(true);
    }

    /**
     * Delete the attached message and destroy the decorator.
     */
    protected final void onDismiss() {
        onDestroy();
        bindTo.delete().complete();
    }
}
