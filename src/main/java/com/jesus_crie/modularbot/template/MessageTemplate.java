package com.jesus_crie.modularbot.template;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;

import java.text.MessageFormat;

public class MessageTemplate extends ModularTemplate<String[], Message> {

    public MessageTemplate(String... lines) {
        super(lines);
    }

    @Override
    public Message format(Object... args) {
        String content = MessageFormat.format(String.join("\n", formatter), args);
        return new MessageBuilder().append(content).build();
    }
}
