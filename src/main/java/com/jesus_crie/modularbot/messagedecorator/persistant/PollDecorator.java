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

/**
 * A decorator that create a poll where users can vote.
 * Persistent by default.
 */
public class PollDecorator extends PersistentDecorator implements Cacheable {

    protected final ConcurrentHashMap<String, Integer> votes = new ConcurrentHashMap<>();

    /**
     * Main constructor.
     * @see com.jesus_crie.modularbot.messagedecorator.persistant.PersistentDecorator#PersistentDecorator(Message, long, boolean, ReactionButton...)
     */
    protected PollDecorator(Message bind, long timeout, boolean resumed, PollButton... buttons) {
        super(bind, timeout, resumed, buttons);

        for (Map.Entry<String, ReactionButton> entry : super.buttons.entrySet())
            votes.put(entry.getKey(), 0);

        listener = Waiter.createListener(((ModularShard) bind.getJDA()), GenericMessageReactionEvent.class,
                e -> isAlive && e.getMessageIdLong() == bindTo.getIdLong(),
                this::vote, () -> destroy(true),
                timeout, false);

        if (callback != null) callback.onReady(this);
    }

    /**
     * Triggered when a reaction is added or removed.
     * @param event the event.
     */
    protected void vote(GenericMessageReactionEvent event) {
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

    /**
     * Get an unmodifiable copy of the votes.
     * @return a copy of the actual votes.
     */
    public Map<String, Integer> getVotes() {
        return Collections.unmodifiableMap(votes);
    }

    // Serialization stuff

    /**
     * Constructor used to deserialize this decorator.
     */
    protected PollDecorator(Message bind, JsonNode node) {
        this(bind,
                node.get("expire_at").asLong() == 0 ? 0 : node.get("expire_at").asLong() - System.currentTimeMillis(),
                true,
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

    /**
     * Represent the button of a poll (with an empty listener).
     */
    public static final class PollButton extends ReactionButton {

        public PollButton(String unicode) {
            super(unicode, EMPTY_LISTENER);
        }

        public PollButton(Emote emote) {
            super(emote, EMPTY_LISTENER);
        }
    }

    /**
     * The builder of this decorator.
     */
    public static final class PollBuilder extends ReactionDecoratorBuilder.DecoratorGlobalBuilder<PollBuilder, PollDecorator> {

        protected long timeout = 0;
        protected final List<PollButton> choices = new ArrayList<>();

        /**
         * Add a choice to this poll with a unicode emote.
         * @param unicode the unicode emote.
         * @return the current builder.
         */
        public PollBuilder addChoice(String unicode) {
            choices.add(new PollButton(unicode));
            return this;
        }

        /**
         * Add a custom emote to this poll.
         * @param emote the guild emote.
         * @return the current builder.
         */
        public PollBuilder addChoice(Emote emote) {
            choices.add(new PollButton(emote));
            return this;
        }

        /**
         * Add multiples unicode emotes to this poll.
         * @param unicodes the emotes.
         * @return the current builder.
         */
        public PollBuilder addChoices(String... unicodes) {
            for (String unicode : unicodes) addChoice(unicode);
            return this;
        }

        /**
         * Add multiple guild emotes to this poll.
         * @param emotes the guild emotes.
         * @return the current builder.
         */
        public PollBuilder addChoices(Emote... emotes) {
            for (Emote emote : emotes) addChoice(emote);
            return this;
        }

        /**
         * @see ReactionDecoratorBuilder#useTimeout(long)
         */
        @Override
        public PollBuilder useTimeout(long timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * Create a new instance of {@link PollDecorator} with the given message and the
         * stored parameters.
         * @param bind the targeted message.
         * @return a new instance of {@link PollDecorator}.
         */
        @Override
        public PollDecorator bindAndBuild(Message bind) {
            return new PollDecorator(bind, timeout, false, choices.toArray(new PollButton[choices.size()]));
        }
    }
}
