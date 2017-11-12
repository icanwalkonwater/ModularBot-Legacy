package com.jesus_crie.modularbot.messagedecorator.persistant;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.jesus_crie.modularbot.messagedecorator.Cacheable;
import com.jesus_crie.modularbot.messagedecorator.ReactionButton;
import net.dv8tion.jda.core.entities.Message;

import java.io.IOException;

public class PanelDecorator extends PersistentDecorator implements Cacheable {

    /**
     * Main constructor
     * @see PersistentDecorator#PersistentDecorator(Message, long, boolean, ReactionButton...)
     */
    protected PanelDecorator(Message bind, long timeout, boolean resumed, ReactionButton... buttons) {
        super(bind, timeout, resumed, buttons);
    }

    @Override
    public void serialize(JsonGenerator gen, SerializerProvider provider) throws IOException {

    }
}
