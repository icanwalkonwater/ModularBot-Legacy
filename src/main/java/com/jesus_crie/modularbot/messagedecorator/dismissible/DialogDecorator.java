package com.jesus_crie.modularbot.messagedecorator.dismissible;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.messagedecorator.ReactionButton;
import com.jesus_crie.modularbot.messagedecorator.ReactionDecoratorBuilder;
import com.jesus_crie.modularbot.sharding.ModularShard;
import com.jesus_crie.modularbot.utils.IgnoreCompletableFuture;
import com.jesus_crie.modularbot.utils.Waiter;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.utils.Checks;

import java.io.IOException;

@JsonSerialize(using = DialogDecorator.DialogSerializer.class)
public class DialogDecorator extends DismissibleDecorator {

    /**
     * The button used to confirm.
     * The emote used is "✅".
     */
    protected static ReactionButton ACCEPT_BUTTON = new ReactionButton("\u2705", (event, decorator) -> ((DialogDecorator) decorator).onTrigger(true));
    /**
     * The button used to deny.
     * The emote used is "❎".
     */
    protected static ReactionButton DENY_BUTTON = new ReactionButton("\u274E", (event, decorator) -> ((DialogDecorator) decorator).onTrigger(false));

    protected final CompletableFuture completable;

    /**
     * Main constructor
     * See {@link DialogBuilder} for more details.
     */
    protected DialogDecorator(Message bind, User target, long timeout) {
        super(bind, target, timeout, ACCEPT_BUTTON, DENY_BUTTON);
        completable = new CompletableFuture();

        listener = Waiter.createListener(((ModularShard) bind.getJDA()), MessageReactionAddEvent.class,
                e -> e.getMessageIdLong() == bind.getIdLong() && e.getUser().equals(target),
                this::onClick, () -> onTrigger(null),
                timeout, true);
    }

    /**
     * Triggered when one of the 2 buttons are pressed or when the timeout is reached.
     * @param res the result, true/false if the dialog has been triggered otherwise null.
     */
    protected void onTrigger(Boolean res) {
        if (res == null) onDestroy();
        else onDismiss();
        completable.complete(res);
    }

    /**
     * Get the result of the dialog in a blocking way.
     * @return the answer of the user or null if the timeout has been reached.
     */
    public Boolean get() {
        return completable.get();
    }

    private static class CompletableFuture extends IgnoreCompletableFuture<Boolean> {}

    /**
     * The builder for this decorator.
     */
    public static class DialogBuilder extends ReactionDecoratorBuilder.DecoratorTargetBuilder<DialogBuilder, DialogDecorator> {

        private long timeout = 60000L;

        /**
         * (Recommended) If you use a timeout, the dialog will automatically return if the user is not responding.
         * If the timeout is reached, the dialog will return null.
         * By default, 1 minute.
         * Use 0 for infinite (not recommended).
         * @param timeout the timeout in milliseconds.
         * @return the current builder.
         */
        @Override
        public DialogBuilder useTimeout(long timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * Create a new {@link DialogDecorator} based on this builder.
         * @param bind the targeted message.
         * @param target the targeted user.
         * @return a new instance of {@link DialogDecorator}.
         */
        @Override
        public DialogDecorator bindAndBuild(Message bind, User target) {
            Checks.notNull(bind, "message");
            Checks.notNull(target, "target");
            return new DialogDecorator(bind, target, timeout);
        }
    }

    /**
     * Serializer used to cache this decorator.
     */
    public static final class DialogSerializer extends StdSerializer<DialogDecorator> {

        public DialogSerializer() {
            super(DialogDecorator.class);
        }

        @Override
        public void serialize(DialogDecorator value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            gen.writeStringField("@class", DialogDecorator.class.getName());
            gen.writeNumberField("message", value.getMessage().getIdLong());

            String source;
            switch (value.getMessage().getChannelType()) {
                case TEXT:
                    source = "G" + value.getMessage().getGuild().getIdLong();
                    break;
                case PRIVATE:
                    source = "P" + value.getMessage().getPrivateChannel().getIdLong();
                    break;
                default:
                    source = "?";
            }
            gen.writeStringField("source", source);

            gen.writeNumberField("user_target", value.getTarget().getIdLong());
            gen.writeNumberField("expire_at", value.getExpireTime());
            gen.writeEndObject();
        }
    }

    public static final class DialogDeserializer extends StdDeserializer<DialogDecorator> {

        public DialogDeserializer() {
            super(DialogDecorator.class);
        }

        @Override
        public DialogDecorator deserialize(JsonParser p, DeserializationContext context) throws IOException, JsonProcessingException {
            JsonNode node = p.getCodec().readTree(p);
            long msgId = node.get("message").asLong();
            String source = node.get("source").asText();
            long userId = node.get("user_target").asLong();
            long expire = node.get("expire_at").asLong();

            ModularBot bot = ModularBot.instance();
            MessageChannel channel;
            switch (source.charAt(0)) {
                case 'G':
                    channel = bot.getTextChannelById(source.substring(1));
            }

            return null;
        }

    }
}
