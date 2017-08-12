package com.jesus_crie.modularbot.stats;

import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.ModularBuilder;
import net.dv8tion.jda.core.events.Event;

import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Stats {

    private static boolean enable = false;

    private static final long start = System.currentTimeMillis();
    private static AtomicInteger commandExecuted = new AtomicInteger(0);
    private static AtomicInteger jdaEvent = new AtomicInteger(0);

    /**
     * Use to reset all of stored stats.
     * Do not affect start time and exterior information like processors count, memory, ect...
     */
    public static void reset() {
        commandExecuted = new AtomicInteger(0);
        jdaEvent = new AtomicInteger(0);
    }

    /**
     * Do not use !
     * Used to count how many commands have been executed.
     * Automatically called when a command is triggered.
     */
    public static void incrementCommand() {
        commandExecuted.incrementAndGet();
    }

    /**
     * Do not use !
     * Only used to count events in one place: {@link com.jesus_crie.modularbot.manager.ModularEventManager#handle(Event)}.
     */
    public static void incrementJDAEvent() {
        jdaEvent.incrementAndGet();
    }

    /**
     * Use to enable the stat system.
     */
    public static void enable() {
        enable = true;
    }

    /**
     * Use to disable the stat system.
     */
    public static void disable() {
        enable = false;
    }

    /**
     * Check if the stat system has been enable in {@link ModularBuilder#useStats()}.
     * @return true if the stat system is enable.
     */
    public static boolean isEnable() {
        return enable;
    }

    /**
     * Create a bundle that contains unmodifiable stats about all lot of things.
     * If you have a lot of guild, this can take a lot of time and memory, use with caution.
     * @return A {@link Bundle} that contains unmodifiable values.
     */
    public static Bundle toBundle() {
        return new Bundle(commandExecuted.get(),
                jdaEvent.get(),
                ModularBot.instance().collectCumulativeShardInfos(
                        shard -> shard.getGuilds().size(),
                        Collectors.summingInt(i -> (int) i)
                ),
                ModularBot.instance().collectCumulativeShardInfos(
                        shard -> shard.getUsers().size(),
                        Collectors.summingInt(i -> (int) i)
                ),
                ManagementFactory.getThreadMXBean().getThreadCount(),
                ModularBot.instance().getShards().size(),
                ModularBot.instance().collectCumulativeShardInfos(
                        shard -> shard.getGuilds().stream()
                                .mapToInt(g -> g.getAudioManager().isConnected() ? 1 : 0)
                                .sum(),
                        Collectors.summingInt(i -> (int) i)
                ),
                Runtime.getRuntime().freeMemory(),
                Runtime.getRuntime().maxMemory(),
                Runtime.getRuntime().availableProcessors());
    }

    /**
     * A bundle of Stats that can be used to be saved.
     */
    public static class Bundle {

        /**
         * The amount of milliseconds since the start of the application.
         */
        public final long UPTIME;

        /**
         * The amount of command that have been executed or that have crashed.
         */
        public final int COMMAND_EXECUTED;

        /**
         * The total amount of jda event received.
         */
        public final int JDA_EVENT;

        /**
         * The total amount of guild across all guilds.
         */
        public final int TOTAL_GUILD;

        /**
         * The total amount of unique users of each shard.
         * If an user is in 2 guild which are in 2 different shards, he will be counted twice.
         */
        public final int TOTAL_USERS;

        /**
         * The amount of threads of the application.
         */
        public final int THREADS;

        /**
         * The amount of shards.
         */
        public final int SHARD_COUNT;

        /**
         * The amount of open audio connections in all guilds.
         */
        public final int AUDIO_CONNECTION;

        /**
         * The amount of memory that is currently used by the JVM.
         */
        public final long MEMORY_USED;

        /**
         * The total amount of memory that can be used.
         */
        public final long MEMORY_TOTAL;

        /**
         * The amount of processors available.
         */
        public final int CPU_AVAILABLE;

        /**
         * Used to create a bundle.
         */
        private Bundle(int cmd, int jda, Integer guild, Integer users, int threads, int shard, int audio, long memTotal, long memUsed, int procs) {
            UPTIME = System.currentTimeMillis() - start;
            COMMAND_EXECUTED = cmd;
            JDA_EVENT = jda;
            TOTAL_GUILD = guild;
            TOTAL_USERS = users;
            THREADS = threads;
            SHARD_COUNT = shard;
            AUDIO_CONNECTION = audio;
            MEMORY_USED = memUsed;
            MEMORY_TOTAL = memTotal;
            CPU_AVAILABLE = procs;
        }
    }
}
