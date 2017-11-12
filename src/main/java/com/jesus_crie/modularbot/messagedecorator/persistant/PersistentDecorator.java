package com.jesus_crie.modularbot.messagedecorator.persistant;

import com.jesus_crie.modularbot.messagedecorator.ReactionButton;
import com.jesus_crie.modularbot.messagedecorator.ReactionDecorator;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

/**
 * A decorator to interact with multiple users and can't be dismiss like this.
 */
public abstract class PersistentDecorator extends ReactionDecorator {

    /**
     * @see ReactionDecorator#ReactionDecorator(Message, User, long, boolean, ReactionButton...)
     */
    protected PersistentDecorator(Message bind, long timeout, boolean resumed, ReactionButton... buttons) {
        super(bind, null, timeout, resumed, buttons);
    }
}
