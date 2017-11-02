package com.jesus_crie.modularbot.messagedecorator.persistant;

import com.jesus_crie.modularbot.messagedecorator.ReactionButton;
import com.jesus_crie.modularbot.messagedecorator.ReactionDecorator;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

public abstract class PersistantDecorator extends ReactionDecorator {

    /**
     * Create a new decorator with some buttons and automatically register it.
     *
     * @param bind    the message to bind to.
     * @param target  the targeted user.
     * @param buttons the buttons to add to the decorator.
     */
    protected PersistantDecorator(Message bind, User target, ReactionButton... buttons) {
        super(bind, target, buttons);
    }
}
