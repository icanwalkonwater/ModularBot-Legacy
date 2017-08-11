package com.jesus_crie.modularbot.utils;

import com.jesus_crie.modularbot.sharding.ModularShard;

import java.util.concurrent.ThreadFactory;

public class ModularThreadFactory implements ThreadFactory {

    private final String identifier;
    private final boolean isDaemon;

    public ModularThreadFactory(ModularShard shard, String name, boolean isDaemon) {
        this.isDaemon = isDaemon;
        identifier = shard.getIdentifierString() + " " + name;
    }

    @Override
    public Thread newThread(Runnable r) {
        final Thread thread = new Thread(r);
        thread.setName(identifier + " #" + thread.getId());
        thread.setDaemon(isDaemon);
        return thread;
    }
}
