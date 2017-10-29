package com.jesus_crie.modularbot.messagedecorator;

import com.jesus_crie.modularbot.utils.MiscUtils;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.utils.Checks;

import java.util.function.BiConsumer;

public class ReactionButton {

    private final String unicode;
    private final OnReactionListener action;

    public ReactionButton(String unicode, OnReactionListener action) {
        Checks.notNull(action, "action");
        MiscUtils.checkEncodableUTF8(unicode);

        this.unicode = unicode;
        this.action = action;
    }

    public String getUnicode() {
        return unicode;
    }

    public void onClick(MessageReactionAddEvent event, ReactionDecorator parent) {
        action.accept(event, parent);
    }

    @FunctionalInterface
    public interface OnReactionListener extends BiConsumer<MessageReactionAddEvent, ReactionDecorator> {}
}
