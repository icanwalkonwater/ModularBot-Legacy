package com.jesus_crie.modularbot.utils.notification;

import com.jesus_crie.modularbot.sharding.ModularShard;
import com.jesus_crie.modularbot.utils.Waiter;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;

public class ModularNotification {

    protected final ModularShard shard;
    protected final MessageChannel channel;
    protected final User notifyTo;
    protected final String emoteRemove;
    protected final boolean dismissible;
    protected final boolean autoDismiss;
    protected final long timeout;

    private Message target;

    public ModularNotification(final ModularShard shard,
                               final MessageChannel channel,
                               final User notifyTo,
                               final String emoteRemove,
                               final boolean dismissible,
                               final boolean autoDismiss,
                               final long timeout) {
        this.shard = shard;
        this.channel = channel;
        this.notifyTo = notifyTo;
        this.emoteRemove = emoteRemove;
        this.dismissible = dismissible;
        this.autoDismiss = autoDismiss;
        this.timeout = timeout;
    }

    public void send(MessageEmbed toSend) {
        send(new MessageBuilder().setEmbed(toSend).build());
    }

    public void send(Message toSend) {
        shard.getGeneralPool().execute(() -> {
            target = channel.sendMessage(toSend).complete();

            try {
                if (dismissible)
                    target.addReaction(emoteRemove).complete();
            } catch (Exception ignore) {
            }

            boolean hasTimeout = Waiter.getNextEvent(shard, MessageReactionAddEvent.class, e -> {
                if (e.getMessageIdLong() != target.getIdLong())
                    return false;

                if (dismissible) {
                    if (notifyTo == null)
                        return e.getReactionEmote().getName().equals(emoteRemove);
                    else
                        return e.getUser().equals(notifyTo) && e.getReactionEmote().getName().equals(emoteRemove);
                }
                return false;
            }, timeout) == null;

            try {
                target.clearReactions().complete();
            } catch (Exception ignore) {}

            if (!hasTimeout || autoDismiss)
                remove();
        });
    }

    public void remove() {
        try {
            if (target != null)
                target.delete().queue();
        } catch (Exception ignore) {}
    }
}
