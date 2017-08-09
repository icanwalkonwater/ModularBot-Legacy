package com.jesus_crie.modularbot.manager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import static com.jesus_crie.modularbot.utils.F.f;

public class ThreadManager {

    private static ExecutorService generalPool;
    private static ExecutorService commandPool;
    private static ScheduledExecutorService timerPool;

    public static void init(String appName) {
        generalPool = Executors.newCachedThreadPool(new NamedFactory(f("%s-General", appName)));
        commandPool = Executors.newCachedThreadPool(new NamedFactory(f("%s-Command", appName)));
        timerPool = Executors.newScheduledThreadPool(0);
    }

    public static ExecutorService getGeneralPool() {
        return generalPool;
    }

    public static ExecutorService getCommandPool() {
        return commandPool;
    }

    public static ScheduledExecutorService getTimerPool() {
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
            thread.setName(f("%s#%d", name, thread.getId()));
            return thread;
        }
    }
}
