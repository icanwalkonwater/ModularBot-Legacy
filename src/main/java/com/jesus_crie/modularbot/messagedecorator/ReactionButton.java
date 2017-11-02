package com.jesus_crie.modularbot.messagedecorator;

import com.jesus_crie.modularbot.utils.MiscUtils;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.utils.Checks;

import java.util.function.BiConsumer;

public class ReactionButton {

    private String unicode;
    private Emote emote;
    private final OnReactionListener action;

    /**
     * Create a new button.
     * @param unicode the unicode string of the emote that will be added to the message.
     * @param action the action that will be ran when the button will be clicked.
     */
    public ReactionButton(String unicode, OnReactionListener action) {
        Checks.notNull(action, "action");
        MiscUtils.checkEncodableUTF8(unicode);

        this.unicode = unicode;
        this.action = action;
    }

    /**
     * Create a new button.
     * @param emote the custom {@link Emote} that will be used.
     * @param action the action that will be performed.
     */
    public ReactionButton(Emote emote, OnReactionListener action) {
        Checks.notNull(action, "action");
        Checks.notNull(emote, "emote");

        this.emote = emote;
        this.action = action;
    }

    /**
     * Get the unicode string corresponding to the emote.
     * @return a string representing the emote that will be used.
     */
    public String getEmoteString() {
        return isCustomEmote() ? emote.getId() : unicode;
    }

    public boolean isCustomEmote() {
        return unicode == null;
    }

    public void setupEmote(Message message) {
        if (unicode == null) message.addReaction(emote).complete();
        else message.addReaction(unicode).complete();
    }

    public boolean checkEmote(MessageReaction.ReactionEmote emote) {
        return isCustomEmote() ? emote.getEmote().getIdLong() == this.emote.getIdLong() : emote.getName().equals(unicode);
    }

    /**
     * The action that will be performed if the button is clicked.
     * @param event the event that has triggered the button.
     * @param parent the decorator that hold this button.
     */
    public void onClick(MessageReactionAddEvent event, ReactionDecorator parent) {
        action.accept(event, parent);
    }

    @FunctionalInterface
    public interface OnReactionListener extends BiConsumer<MessageReactionAddEvent, ReactionDecorator> {}
}
