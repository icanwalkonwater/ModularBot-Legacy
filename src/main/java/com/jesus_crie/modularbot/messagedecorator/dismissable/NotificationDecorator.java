package com.jesus_crie.modularbot.messagedecorator.dismissable;

import com.jesus_crie.modularbot.messagedecorator.ReactionButton;
import com.jesus_crie.modularbot.sharding.ModularShard;
import com.jesus_crie.modularbot.utils.Waiter;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;

public class NotificationDecorator extends DismissibleDecorator {

    public static final String RED_CROSS = "\u274C";

    public NotificationDecorator(Message bind, User target, long timeout) {
        super(bind, target, new ReactionButton(RED_CROSS, (e, d) -> d.onDestroy()));

        listener = Waiter.createListener(((ModularShard) bind.getJDA()),
                MessageReactionAddEvent.class,
                e -> e.getMessageIdLong() == bind.getIdLong() && e.getUser().equals(target),
                this::onClick, this::onDestroy,
                timeout, true);
    }
}
