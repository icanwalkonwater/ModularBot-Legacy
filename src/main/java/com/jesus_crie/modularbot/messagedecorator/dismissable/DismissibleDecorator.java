package com.jesus_crie.modularbot.messagedecorator.dismissable;

import com.jesus_crie.modularbot.messagedecorator.ReactionButton;
import com.jesus_crie.modularbot.messagedecorator.ReactionDecorator;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.utils.Checks;

public abstract class DismissibleDecorator extends ReactionDecorator {

    protected final ReactionButton dismissButton;

    public DismissibleDecorator(Message bind, User target, ReactionButton dismissButton, ReactionButton... buttons) {
        super(bind, target, buttons);
        Checks.notNull(dismissButton, "dismissButton");

        this.dismissButton = dismissButton;
        bind.addReaction(dismissButton.getUnicode()).complete();
    }

    @Override
    protected void onClick(MessageReactionAddEvent event) {
        if (event.getReactionEmote().getName().equals(dismissButton.getUnicode())) onDismiss();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        listener.cancel(true);
    }

    protected void onDismiss() {
        onDestroy();
        bindTo.delete().complete();
    }
}
