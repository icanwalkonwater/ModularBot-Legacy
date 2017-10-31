package com.jesus_crie.modularbot.listener;

import com.jesus_crie.modularbot.sharding.ModularShard;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.hooks.EventListener;

import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class WaiterListener<T extends Event> extends CompletableFuture<T> implements EventListener {

    public static final WaiterListener EMPTY = new WaiterListener<>();

    private final ModularShard shard;
    private final Class<T> eventType;
    private Predicate<T> onTrigger = null;

    public WaiterListener(final ModularShard shard, final Class<T> eventType) {
        this.shard = shard;
        this.eventType = eventType;
        shard.addEventListener(this);
    }

    private WaiterListener() {
        shard = null;
        eventType = null;
    }

    public void setOnTrigger(Predicate<T> onTrigger) {
        this.onTrigger = onTrigger;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onEvent(Event event) {
        if (event.getClass().getName().equals(eventType.getName())) {
            if (onTrigger == null)
                return;

            if (onTrigger.test((T) event)) {
                unregister();
                complete((T) event);
            }
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        unregister();
        return super.cancel(mayInterruptIfRunning);
    }

    public void unregister() {
        if (shard == null) return;
        shard.removeEventListener(this);
    }
}
