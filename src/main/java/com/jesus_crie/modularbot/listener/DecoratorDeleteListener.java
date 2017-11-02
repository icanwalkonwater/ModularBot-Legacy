package com.jesus_crie.modularbot.listener;

import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.messagedecorator.ReactionDecorator;
import net.dv8tion.jda.core.events.message.MessageDeleteEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

/**
 * Used to automatically destroy a decorator if his targeted message is deleted.
 */
public class DecoratorDeleteListener extends ListenerAdapter {

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        ReactionDecorator decorator = ModularBot.getDecoratorManager().getDecoratorForMessage(event.getMessageIdLong());
        if (decorator != null) decorator.destroy();
    }
}
