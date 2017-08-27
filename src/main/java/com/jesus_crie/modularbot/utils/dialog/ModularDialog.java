package com.jesus_crie.modularbot.utils.dialog;

import com.jesus_crie.modularbot.sharding.ModularShard;
import com.jesus_crie.modularbot.utils.Waiter;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;

import java.util.concurrent.CompletableFuture;

public class ModularDialog extends CompletableFuture<Boolean> {

    /**
     * Use {@link DialogBuilder}.
     * @see DialogBuilder
     */
    ModularDialog(final ModularShard shard,
                  final Message message,
                  final User author,
                  final String unicodeAccept,
                  final String unicodeDeny,
                  final long timeout,
                  final boolean deleteAfter) {
        try {
            message.addReaction(unicodeAccept).complete();
            message.addReaction(unicodeDeny).complete();

            final MessageReactionAddEvent event = Waiter.getNextEvent(shard, MessageReactionAddEvent.class,
                    e -> e.getMessageIdLong() == message.getIdLong()
                            && e.getUser().getIdLong() == author.getIdLong()
                            && (e.getReactionEmote().getName().equals(unicodeAccept)
                            || e.getReactionEmote().getName().equals(unicodeDeny)), timeout);

            if (deleteAfter)
                message.delete().queue();
            else
                message.clearReactions().queue();

            if (event != null)
                if (event.getReactionEmote().getName().equals(unicodeAccept))
                    complete(true);
                else
                    complete(false);
            else
                complete(null);
        } catch (Exception ignore) {}
    }

    private void perform() {

    }

    /**
     * Get the result of the dialog in a blocking way, without handling the exception.
     * @return true if the user has responded yes, false if the user has responded no, null in case of timeout or when an exception is throw.
     */
    @Override
    public Boolean get() {
        try {
            return super.get();
        } catch (Exception e) {
            return null;
        }
    }
}
