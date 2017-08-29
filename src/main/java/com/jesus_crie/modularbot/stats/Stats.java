package com.jesus_crie.modularbot.stats;

import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.ModularBuilder;
import com.jesus_crie.modularbot.stats.bundle.Bundle;
import com.jesus_crie.modularbot.stats.bundle.BundleBuilder;
import com.jesus_crie.modularbot.stats.bundle.Keys;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.GuildVoiceState;
import net.dv8tion.jda.core.events.Event;

import java.lang.management.ManagementFactory;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Stats {

    private static boolean enable = false;

    private static final long start = System.currentTimeMillis();
    private static final AtomicInteger commandExecuted = new AtomicInteger(0);
    private static final AtomicInteger jdaEvent = new AtomicInteger(0);
    private static final ConcurrentHashMap<String, AtomicInteger> commands = new ConcurrentHashMap<>();

    /**
     * Use to reset all of stored stats.
     * Do not affect start time and exterior information like processors count, memory, ect...
     */
    public static void reset() {
        commandExecuted.set(0);
        jdaEvent.set(0);
        commands.clear();
        ModularBot.getCommandManager().getCommands().forEach(c -> commands.put(c.getName(), new AtomicInteger(0)));
    }

    /**
     * Do not use !
     * Used to count how many commands have been executed.
     * Automatically called when a command is triggered.
     */
    public static void incrementCommand(String name) {
        commandExecuted.incrementAndGet();

        // Can be optimized.
        commands.putIfAbsent(name, new AtomicInteger(0)); // If a command was registered between reset() and this method.
        commands.get(name).incrementAndGet();
    }

    /**
     * Do not use !
     * Only used to count events in one place: {@link com.jesus_crie.modularbot.manager.ModularEventManager#handle(Event)}.
     */
    public static void incrementJDAEvent() {
        jdaEvent.incrementAndGet();
    }

    /**
     * Use to enable/disable the stat system.
     */
    public static void setEnable(boolean state) {
        enable = state;
    }

    /**
     * Check if the stat system has been enable in {@link ModularBuilder#useStats()}.
     * @return true if the stat system is enable.
     */
    public static boolean isEnable() {
        return enable;
    }

    /**
     * Create a bundle that contains unmodifiable stats about a lot of things across the shards.
     * If you have a lot of guild, this can take a lot of time and memory, use with caution.
     * @return a {@link Bundle} that contains unmodifiable values.
     */
    public static Bundle collectGlobal() {
        return new BundleBuilder()
                .append(Keys.COMMAND_EXECUTED, commandExecuted.get())
                .append(Keys.JDA_EVENT, jdaEvent.get())
                .append(Keys.TOTAL_GUILD, ModularBot.instance().collectCumulativeShardInfos(s -> s.getGuilds().size(), Collectors.summingInt(i -> (int) i)))
                .append(Keys.TOTAL_USERS, ModularBot.instance().collectCumulativeShardInfos(s -> s.getUsers().size(), Collectors.summingInt(i -> (int) i)))
                .append(Keys.THREAD_COUNT, ManagementFactory.getThreadMXBean().getThreadCount())
                .append(Keys.SHARD_COUNT, ModularBot.instance().getShards().size())
                .append(Keys.TEXT_CHANNEL_COUNT, ModularBot.instance().collectCumulativeShardInfos(s -> s.getTextChannels().size(), Collectors.summingInt(i -> (int) i)))
                .append(Keys.VOICE_CHANNEL_COUNT, ModularBot.instance().collectCumulativeShardInfos(s -> s.getVoiceChannels().size(), Collectors.summingInt(i -> (int) i)))
                .append(Keys.AUDIO_CONNECTION, !ModularBot.isAudioEnabled() ? 0 :
                                ModularBot.instance().collectCumulativeShardInfos(s -> s.getGuilds().stream().mapToInt(g -> g.getAudioManager().isConnected() ? 1 : 0).sum(),
                                Collectors.summingInt(i -> (int) i)))
                .append(Keys.FREE_MEMORY, Runtime.getRuntime().freeMemory())
                .append(Keys.MAX_MEMORY, Runtime.getRuntime().maxMemory())
                .append(Keys.CPU_AVAILABLE, Runtime.getRuntime().availableProcessors())
                .build();
    }

    /**
     * Collect information about a guild to a {@link Bundle}.
     * @param guild the source guild.
     * @return a {@link Bundle} containing data about the given guild.
     */
    public static Bundle collectGuild(Guild guild) {
        return new BundleBuilder()
                .append(Keys.GUILD_MEMBERS, guild.getMembers().size())
                .append(Keys.GUILD_MEMBERS_CONNECTED, guild.getMembers().stream().filter(m -> m.getOnlineStatus() != OnlineStatus.OFFLINE).count())
                .append(Keys.GUILD_EMOTE_COUNT, guild.getEmotes().size())
                .append(Keys.GUILD_VOICE_CONNECTED, guild.getVoiceStates().stream().filter(GuildVoiceState::inVoiceChannel).count())
                .build();
    }

    /**
     * Collect a {@link Bundle} containing how many times a command has been triggered.
     * The keys are made like this: "COMMAND_{name}" in uppercase.
     * For example the key for the "help" command will be "COMMAND_HELP".
     * @return a {@link Bundle} with information on how many times a command has been triggered.
     */
    public static Bundle collectCommands() {
        final BundleBuilder builder = new BundleBuilder();
        commands.forEach((k, v) -> builder.append("COMMAND_" + k.toUpperCase(), v.get()));
        return builder.build();
    }

    /**
     * Collect every possible data about the bot to a {@link Bundle}.
     * This include global stats that are accessible at the root and a bundle
     * per guild accessible using the key "GUILD_{id}" to get a sub bundle.
     *
     * If you use this periodically for graph remember to use {@link #reset()}.
     * @return a {@link Bundle} containing global stats and a sub bundle for each guild.
     */
    public static Bundle collectEverything() {
        final BundleBuilder builder = new BundleBuilder();
        builder.merge(collectGlobal());
        builder.merge(collectCommands());
        ModularBot.instance().dispatchCommand(s -> s.getGuilds().forEach(g -> builder.append("GUILD_" + g.getIdLong(), collectGuild(g))));

        return builder.build();
    }
}
