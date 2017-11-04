package com.jesus_crie.modularbot.messagedecorator.persistant;

import com.jesus_crie.modularbot.messagedecorator.ReactionButton;
import com.jesus_crie.modularbot.messagedecorator.ReactionDecoratorBuilder;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PollDecorator extends PersistantDecorator {

    protected final ConcurrentHashMap<String, Integer> votes = new ConcurrentHashMap<>();

    protected PollDecorator(Message bind, long timeout, PollButton... buttons) {
        super(bind, timeout, buttons);

        for (Map.Entry<String, ReactionButton> entry : super.buttons.entrySet())
            votes.put(entry.getKey(), 0);
    }

    @Override
    protected ReactionButton onClick(MessageReactionAddEvent event) {
        PollButton button = (PollButton) super.onClick(event);
        votes.put(button.getEmoteString(), votes.get(button.getEmoteString()) + 1);
        return button;
    }

    public Map<String, Integer> getVotes() {
        return Collections.unmodifiableMap(votes);
    }

    public static final class PollButton extends ReactionButton {

        public PollButton(String unicode) {
            super(unicode, null);
        }

        public PollButton(Emote emote) {
            super(emote, null);
        }
    }

    public static final class PollBuilder extends ReactionDecoratorBuilder.DecoratorGlobalBuilder<PollBuilder, PollDecorator> {

        protected long timeout = 0;
        protected final List<PollButton> choices = new ArrayList<>();

        public PollBuilder addChoice(String unicode) {
            choices.add(new PollButton(unicode));
            return this;
        }

        public PollBuilder addChoice(Emote emote) {
            choices.add(new PollButton(emote));
            return this;
        }

        public PollBuilder addChoices(String... unicodes) {
            for (String unicode : unicodes) addChoice(unicode);
            return this;
        }

        public PollBuilder addChoices(Emote... emotes) {
            for (Emote emote : emotes) addChoice(emote);
            return this;
        }

        @Override
        public PollBuilder useTimeout(long timeout) {
            this.timeout = timeout;
            return this;
        }

        @Override
        public PollDecorator bindAndBuild(Message bind) {
            return new PollDecorator(bind, timeout);
        }
    }
}
