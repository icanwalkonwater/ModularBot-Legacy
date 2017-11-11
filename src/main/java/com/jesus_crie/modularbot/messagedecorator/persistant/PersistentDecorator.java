package com.jesus_crie.modularbot.messagedecorator.persistant;

import com.jesus_crie.modularbot.messagedecorator.ReactionButton;
import com.jesus_crie.modularbot.messagedecorator.ReactionDecorator;
import net.dv8tion.jda.core.entities.Message;

/**
 * A decorator to interact with multiple users and can't be dismiss like this.
 */
public abstract class PersistentDecorator extends ReactionDecorator {

    protected PersistentDecorator(Message bind, long timeout, ReactionButton... buttons) {
        super(bind, null, timeout, buttons);
    }
}
