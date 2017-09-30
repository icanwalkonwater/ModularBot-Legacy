package com.jesus_crie.modularbot.listener;

import com.jesus_crie.modularbot.utils.IgnoreCompletableFuture;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.EventListener;

public class ReadyListener extends IgnoreCompletableFuture<Void> implements EventListener {

    @Override
    public void onEvent(Event event) {
        if (event instanceof ReadyEvent)
            complete(null);
    }
}
