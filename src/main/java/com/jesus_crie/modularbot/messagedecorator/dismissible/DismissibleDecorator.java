package com.jesus_crie.modularbot.messagedecorator.dismissible;

import com.jesus_crie.modularbot.messagedecorator.ReactionButton;
import com.jesus_crie.modularbot.messagedecorator.ReactionDecorator;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.Checks;

/**
 * A decorator that contains a dismiss button to delete the message.
 */
public abstract class DismissibleDecorator extends ReactionDecorator {

    protected final String unicodeDismiss;

    /**
     * Create a dismissible decorator with a button that will delete the message.
     * @param bind the message to bind to.
     * @param target the targeted user.
     * @param unicodeDismiss the dismiss button.
     * @param buttons the other buttons.
     */
    protected DismissibleDecorator(Message bind, User target, String unicodeDismiss, ReactionButton... buttons) {
        super(bind, target, buttons);
        Checks.notNull(unicodeDismiss, "unicodeDismiss");
        this.unicodeDismiss = unicodeDismiss;

        super.buttons.put(unicodeDismiss, new ReactionButton(unicodeDismiss, (e, d) -> onDismiss()));
        bind.addReaction(unicodeDismiss).complete();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        listener.cancel(true);
    }

    /**
     * Triggered when the dismiss button is triggered.
     */
    protected void onDismiss() {
        onDestroy();
        bindTo.delete().complete();
    }
}
