package com.jesus_crie.modularbot.messagedecorator.persistant;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.jesus_crie.modularbot.messagedecorator.Cacheable;
import com.jesus_crie.modularbot.messagedecorator.ReactionButton;
import com.jesus_crie.modularbot.messagedecorator.ReactionDecoratorBuilder;
import com.jesus_crie.modularbot.sharding.ModularShard;
import com.jesus_crie.modularbot.utils.MiscUtils;
import com.jesus_crie.modularbot.utils.Waiter;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.StreamSupport;

public class PollDecorator extends PersistentDecorator implements Cacheable {

    protected final ConcurrentHashMap<String, Integer> votes = new ConcurrentHashMap<>();

    protected PollDecorator(Message bind, long timeout, PollButton... buttons) {
        super(bind, timeout, buttons);

        for (Map.Entry<String, ReactionButton> entry : super.buttons.entrySet())
            votes.put(entry.getKey(), 0);

        listener = Waiter.createListener(((ModularShard) bind.getJDA()), GenericMessageReactionEvent.class,
                e -> isAlive && e.getMessageIdLong() == bindTo.getIdLong(),
                this::onVote, this::destroy,
                timeout, false);
    }

    protected void onVote(GenericMessageReactionEvent event) {
        String emote = MiscUtils.stringifyEmote(event.getReactionEmote());
        if (votes.containsKey(emote)) {
            int add;
            if (event instanceof MessageReactionAddEvent) add = 1;
            else if (event instanceof MessageReactionRemoveEvent) add = -1;
            else add = 0;
            if (callback != null && callback.onVote(this, event.getReactionEmote(), event.getUser(), add > 0)) return;
            votes.put(emote, votes.get(emote) + add);
        }
    }

    public Map<String, Integer> getVotes() {
        return Collections.unmodifiableMap(votes);
    }

    // Serialization stuff

    protected PollDecorator(Message bind, JsonNode node) {
        this(bind,
                node.get("expire_at").asLong() == 0 ? 0 : node.get("expire_at").asLong() - System.currentTimeMillis(),
                StreamSupport.stream(node.get("votes").spliterator(), false)
                        .map(n -> n.get("is_emote").asBoolean() ? new PollButton(bind.getJDA().getEmoteById(n.get("value").asText()))
                                                                : new PollButton(n.get("value").asText()))
                        .toArray(PollButton[]::new));

        for (MessageReaction reaction : bind.getReactions())
            if (reaction.isSelf()) votes.put(MiscUtils.stringifyEmote(reaction.getReactionEmote()), reaction.getCount() - 1);

        bind.getChannel().sendMessage(votes.toString()).queue();
    }

    @Override
    public void serialize(JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeNumberField("expire_at", getExpireTime());
        gen.writeArrayFieldStart("votes");
        for (Map.Entry<String, ReactionButton> entry : buttons.entrySet()) {
            gen.writeStartObject();
            gen.writeBooleanField("is_emote", entry.getValue().isCustomEmote());
            gen.writeStringField("value", entry.getKey());
            gen.writeEndObject();
        }
        gen.writeEndArray();
    }

    public static final class PollButton extends ReactionButton {

        public PollButton(String unicode) {
            super(unicode, EMPTY_LISTENER);
        }

        public PollButton(Emote emote) {
            super(emote, EMPTY_LISTENER);
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
            return new PollDecorator(bind, timeout, choices.toArray(new PollButton[choices.size()]));
        }
    }
}
