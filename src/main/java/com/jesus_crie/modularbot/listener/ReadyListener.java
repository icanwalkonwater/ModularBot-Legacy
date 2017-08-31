package com.jesus_crie.modularbot.listener;

import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.EventListener;

import java.util.concurrent.CompletableFuture;

public class ReadyListener extends CompletableFuture<Void> implements EventListener {

    @Override
    public void onEvent(Event event) {
        if (event instanceof ReadyEvent)
            complete(null);
    }

    @Override
    public Void get() {
        try {
            return super.get();
        } catch (Exception ignore) {
            return null;
        }
    }
}
