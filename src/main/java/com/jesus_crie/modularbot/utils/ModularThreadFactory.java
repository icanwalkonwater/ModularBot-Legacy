package com.jesus_crie.modularbot.utils;

import com.jesus_crie.modularbot.sharding.ModularShard;

import javax.annotation.Nonnull;
import java.util.concurrent.ThreadFactory;

public class ModularThreadFactory implements ThreadFactory {

    private final String identifier;

    /**
     * The {@link ThreadFactory} that is used for all Thread pools from ModularBot.
     *  @param shard    the {@link ModularShard} that we need to create {@link Thread} for.
     * @param name     the name or category of threads to create (used for logging).
     */
    public ModularThreadFactory(ModularShard shard, String name) {
        identifier = shard.getIdentifierString() + " " + name;
    }

    public ModularThreadFactory(String name) {
        identifier = name;
    }

    /**
     * @see ThreadFactory#newThread(Runnable)
     */
    @Override
    public Thread newThread(@Nonnull final Runnable r) {
        final Thread thread = new Thread(r);
        thread.setName(identifier + " #" + thread.getId());
        thread.setDaemon(true);
        return thread;
    }
}
