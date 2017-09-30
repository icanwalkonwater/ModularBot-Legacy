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
        } catch (Exception ignore) {} // Many try catch because private channels and permissions...
        try {
            message.addReaction(unicodeDeny).complete();
        } catch (Exception ignore) {}

        MessageReactionAddEvent event = null;
        try {
            event = Waiter.getNextEvent(shard, MessageReactionAddEvent.class,
                    e -> e.getMessageIdLong() == message.getIdLong()
                            && e.getUser().getIdLong() == author.getIdLong()
                            && (e.getReactionEmote().getName().equals(unicodeAccept)
                            || e.getReactionEmote().getName().equals(unicodeDeny)), timeout);
        } catch (Exception ignore) {}

        try {
            if (deleteAfter)
                message.delete().queue();
            else
                message.clearReactions().queue();
        } catch (Exception ignore) {}

        if (event != null)
            if (event.getReactionEmote().getName().equals(unicodeAccept))
                complete(true);
            else
                complete(false);
        else
            complete(null);
    }
}
