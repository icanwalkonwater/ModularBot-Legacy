package com.jesus_crie.modularbot.messagedecorator.persistant;

import com.jesus_crie.modularbot.messagedecorator.ReactionButton;
import com.jesus_crie.modularbot.messagedecorator.ReactionDecorator;
import net.dv8tion.jda.core.entities.Message;

/**
 * A decorator to interact with multiple users and can't be dismiss like this.
 */
public abstract class PersistantDecorator extends ReactionDecorator {

    protected PersistantDecorator(Message bind, ReactionButton... buttons) {
        super(bind, null, 0, buttons);
    }
}
