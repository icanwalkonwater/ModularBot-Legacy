package com.jesus_crie.modularbot.messagedecorator;

import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.listener.WaiterListener;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.utils.Checks;

import java.util.HashMap;

public abstract class ReactionDecorator {

    protected final Message bindTo;
    protected final User target;
    protected final HashMap<String, ReactionButton> buttons = new HashMap<>();
    @SuppressWarnings("unchecked")
    protected WaiterListener<MessageReactionAddEvent> listener = WaiterListener.EMPTY;

    protected boolean isAlive = true;

    public ReactionDecorator(Message bind, User target, ReactionButton... buttons) {
        Checks.notNull(bind, "message");

        bindTo = bind;
        this.target = target;
        for (ReactionButton button : buttons) {
            this.buttons.put(button.getUnicode(), button);
            bind.addReaction(button.getUnicode()).complete();
        }

        ModularBot.getDecoratorManager().registerDecorator(this);
    }

    public Message getMessage() {
        return bindTo;
    }

    public boolean isAlive() {
        return isAlive;
    }

    protected abstract void onClick(MessageReactionAddEvent event);

    public void onDestroy() {
        isAlive = false;
        ModularBot.getDecoratorManager().unregister(this);
        bindTo.clearReactions().complete();
    }

    @Override
    public boolean equals(Object obj) {
        return getClass().getName().equals(obj.getClass().getName())
                && bindTo.equals(((ReactionDecorator) obj).bindTo)
                && buttons.size() == ((ReactionDecorator) obj).buttons.size();
    }
}
