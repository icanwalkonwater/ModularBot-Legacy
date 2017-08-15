package com.jesus_crie.modularbot.utils;

import com.jesus_crie.modularbot.sharding.ModularShard;
import com.sun.istack.internal.NotNull;

import java.util.concurrent.ThreadFactory;

public class ModularThreadFactory implements ThreadFactory {

    private final String identifier;
    private final boolean isDaemon;

    /**
     * The {@link ThreadFactory} that is used for all Thread pools from ModularBot.
     * @param shard the {@link ModularShard} that we need to create {@link Thread} for.
     * @param name the name or category of threads to create (used for logging).
     * @param isDaemon if the threads needs to be daemon threads.
     */
    public ModularThreadFactory(ModularShard shard, String name, boolean isDaemon) {
        this.isDaemon = isDaemon;
        identifier = shard.getIdentifierString() + " " + name;
    }

    /**
     * @see ThreadFactory#newThread(Runnable)
     */
    @Override
    public Thread newThread(@NotNull final Runnable r) {
        final Thread thread = new Thread(r);
        thread.setName(identifier + " #" + thread.getId());
        thread.setDaemon(isDaemon);
        return thread;
    }
}
