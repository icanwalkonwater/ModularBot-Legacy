package com.jesus_crie.modularbot.manager;

import java.util.concurrent.*;

import static com.jesus_crie.modularbot.utils.F.f;

public class ThreadManager {

    private ExecutorService generalPool;
    private ExecutorService commandPool;
    private ScheduledExecutorService timerPool;

    public ThreadManager(String appName, int shardId) {
        restart(appName, shardId, false, false);
    }

    public void restart(String appName, int shard, boolean force, boolean cleanFirst) {
        if (cleanFirst)
            cleanup(force);
        generalPool = Executors.newCachedThreadPool(new NamedFactory(f("%s#%d General", appName, shard)));
        commandPool = Executors.newCachedThreadPool(new NamedFactory(f("%s#%d Command", appName, shard)));
        timerPool = Executors.newScheduledThreadPool(0, new NamedFactory(f("%s#%d Timer", appName, shard)));
    }

    public void cleanup(boolean force) {
        if (force) {
            generalPool.shutdownNow();
            commandPool.shutdownNow();
            timerPool.shutdownNow();
        } else {
            try {
                generalPool.awaitTermination(500, TimeUnit.MILLISECONDS);
                commandPool.awaitTermination(500, TimeUnit.MILLISECONDS);
                timerPool.awaitTermination(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignore) {}
        }
    }

    public ExecutorService getGeneralPool() {
        return generalPool;
    }

    public ExecutorService getCommandPool() {
        return commandPool;
    }

    public ScheduledExecutorService getTimerPool() {
        return timerPool;
    }

    private static class NamedFactory implements ThreadFactory {

        private final String name;

        public NamedFactory(String name) {
            this.name = name;
        }

        @Override
        public Thread newThread(Runnable r) {
            final Thread thread = new Thread(r);
            thread.setName(f("%s #%d", name, thread.getId()));
            return thread;
        }
    }
}
