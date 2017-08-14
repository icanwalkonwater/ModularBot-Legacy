package com.jesus_crie.modularbot;

import com.jesus_crie.modularbot.config.ConfigHandler;
import com.jesus_crie.modularbot.listener.CommandHandler;
import com.jesus_crie.modularbot.log.Logger;
import com.jesus_crie.modularbot.manager.CommandManager;
import com.jesus_crie.modularbot.manager.ModularEventManager;
import com.jesus_crie.modularbot.sharding.ModularJDABuilder;
import com.jesus_crie.modularbot.sharding.ModularShard;
import com.jesus_crie.modularbot.utils.MiscUtils;
import com.jesus_crie.modularbot.utils.Status;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.utils.Checks;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.jesus_crie.modularbot.utils.F.f;

/**
 * Can only be instantiated one time.
 */
public class ModularBot {

    private static ModularBot instance;
    private static Logger logger;

    private final String token;
    private final boolean useAudio;
    private int maxShard;

    private final ConfigHandler config;
    private final CommandManager commandManager;
    private final List<ModularShard> shards;

    private final ScheduledThreadPoolExecutor mightyPool;

    /**
     * Package-Private builder.
     * @param token the token provided.
     * @param config a custom {@link ConfigHandler}.
     * @param useAudio if the audio must be enabled.
     */
    ModularBot(String token, ConfigHandler config, Logger logger, CommandHandler command, boolean useAudio) {
        Thread.currentThread().setName(f("%s Main", config.getAppName(), Thread.currentThread().getId()));

        mightyPool = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(0, r -> {
            Thread t = new Thread(r);
            t.setName(f("%s Mighty #%s", config.getAppName(), Thread.currentThread().getId()));
            return t;
        });
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> logger.error("UNCAUGHT EXCEPTION", e));

        Checks.notEmpty(token, "token");
        Checks.notNull(config, "config");
        Checks.notNull(logger, "logger");
        Checks.notNull(command, "command");

        commandManager = new CommandManager(command);

        instance = this;
        shards = new ArrayList<>();
        ModularBot.logger = logger;
        logger.info("Start", "Logger initialized !");

        this.token = token;
        this.config = config;
        this.useAudio = useAudio;

        logger.info("Start", "Loading config...");
        try {
            config.load();
        } catch (IOException e) {
            logger.fatal("Start", "Error while loading config !");
            logger.error("Start", e);
        }
    }

    /**
     * Use to create shards and connect to discord.
     * @throws LoginException if the token is wrong.
     * @throws InterruptedException if something interrupt a login request.
     * @throws RateLimitedException if we are rate-limited.
     */
    public void connectToDiscord() throws LoginException, RateLimitedException, InterruptedException {
        logger.info("Start", "Attempting to spawn and start shards...");
        restartShards();
    }

    /**
     * Used to regenerate every shard of the bot.
     * @throws LoginException if the token is wrong.
     * @throws InterruptedException if something interrupt a login request.
     * @throws RateLimitedException if we are rate-limited.
     */
    public void restartShards() throws LoginException, InterruptedException, RateLimitedException {
        if (!shards.isEmpty()) {
            shards.forEach(s -> {
                s.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Status.STOPPING);
                s.shutdownNow();
            });
        }

        shards.clear();
        maxShard = getShardMax(false);
        logger.info("Start", f("Creation of %s shards is required.", maxShard));

        for (int i = 0; i < maxShard; i++) {
            logger.info("Start", f("Starting shard %s of %s...", i + 1, maxShard));
            shards.add(new ModularJDABuilder(AccountType.BOT)
                    .setToken(token)
                    .useSharding(i, maxShard)
                    .setAutoReconnect(true)
                    .setAudioEnabled(useAudio)
                    .setEventManager(new ModularEventManager())
                    .setIdle(false)
                    .setGame(Status.STARTING)
                    .buildBlocking());
        }

        shards.sort(ModularShard::compareTo);
    }

    /**
     * Shutdown a specific shard and re-instantiate it.
     * @param shardId the id of the shard to restart.
     * @throws LoginException if the token is wrong.
     * @throws InterruptedException if something interrupt a login request.
     * @throws RateLimitedException if we are rate-limited.
     */
    public void restartShardById(int shardId) throws LoginException, InterruptedException, RateLimitedException {
        MiscUtils.requireBetween(0, shards.size() - 1, shardId, "shardId is not valid !");

        logger.info("Start", f("Restarting shard %s of %s", shardId, maxShard));
        shards.get(shardId).shutdownNow();

        shards.set(shardId, new ModularJDABuilder(AccountType.BOT)
                .setToken(token)
                .useSharding(shardId, maxShard)
                .setAutoReconnect(true)
                .setAudioEnabled(useAudio)
                .setEventManager(new ModularEventManager())
                .setIdle(false)
                .setGame(Status.STARTING)
                .buildBlocking());

        shards.sort(ModularShard::compareTo);
    }

    /**
     * Shutdown every shards one by one.
     * @param force if true {@link ModularShard#shutdownNow()} will be used instead of {@link ModularShard#shutdown()}
     */
    public void shutdownShards(boolean force) {
        if (force) {
            shards.forEach(ModularShard::shutdown);
        } else {
            shards.forEach(ModularShard::shutdownNow);
        }
    }

    /**
     * Shutdown all shards, save the config and stop thread pools.
     * @param force if true, shutdown as fast as is possible.
     */
    public void shutdown(boolean force) {
        logger.info("Stop", f("Shutting down %s shards...", shards.size()));
        shutdownShards(force);

        logger.info("Stop", "Trying to save config...");
        try {
            config.save();
        } catch (IOException e) {
            logger.error("Stop", e);
        }

        if (force) {
            mightyPool.shutdownNow();
        } else {
            try {
                mightyPool.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException ignore) {
            } finally {
                mightyPool.shutdown();
            }
        }
    }

    /**
     * Get an unmodifiable list of all shards
     * @return an instance of {@link Collections.UnmodifiableList} that contains a copy of all shards.
     */
    public List<ModularShard> getShards() {
        return Collections.unmodifiableList(shards);
    }

    public ModularShard getShardById(int shardId) {
        return shards.get(shardId);
    }

    /**
     * Used to collect infos from all shards by shards.
     * Mainly used to collect stats.
     * @param action used to get the needed object in each shard.
     * @param <T> the type of Object needed.
     * @return a {@link List} with the needed T object of each shard.
     */
    public <T> List<T> collectShardInfos(Function<ModularShard, T> action) {
        return shards.stream()
                .map(action)
                .collect(Collectors.toList());
    }

    /**
     * Used to collect infos and add them together in a single object.
     * Mainly used to collect stats.
     * @param action used to get the needed object in each shard.
     * @param mapper used to merge each object into one.
     * @param <T> the type of Object needed.
     * @return an object T with contains merged data from each shard.
     */
    public <T> T collectCumulativeShardInfos(Function<ModularShard, T> action, Collector<T, ?, T> mapper) {
        return shards.stream()
                .map(action)
                .collect(mapper);
    }

    /**
     * Query the recommended amount of shards from the discord API.
     * @return the recommended amount of shards.
     */
    private int getShardMax(boolean debug) {
        if (debug)
            return 3;

        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("https://discordapp.com/api/gateway/bot")
                    .addHeader("Authorization", "Bot " + token)
                    .addHeader("Content-Type", "application/json")
                    .get()
                    .build();
            Response res = client.newCall(request).execute();
            JSONObject j = new JSONObject(res.body().string());
            res.close();

            return j.getInt("shards");
        } catch (IOException ignore) {}

        return 1;
    }

    /**
     * Get the main {@link ScheduledThreadPoolExecutor} that don't depend of any shard.
     * Need to be used for global operation.
     * @return the might thread pool.
     */
    public ScheduledThreadPoolExecutor getMightyPool() {
        return mightyPool;
    }

    @Override
    public String toString() {
        return f("%s v%s #%s", config.getAppName(), config.getVersion().toString(), hashCode());
    }

    /**
     * Get the current instance of {@link ModularBot}.
     * @return a possibly-null instance of {@link ModularBot} (if called during the constructor).
     */
    public static ModularBot instance() {
        return instance;
    }

    /**
     * Get the current implementation of the {@link Logger} that is used by the application.
     * @return an implementation of {@link Logger}.
     */
    public static Logger logger() {
        return logger;
    }

    /**
     * Get the config handler of the application.
     * @return an implementation of {@link ConfigHandler} that is currently used by the application.
     */
    public static ConfigHandler getConfig() {
        return instance.config;
    }

    /**
     * Get the command manager of the application.
     * @return the {@link CommandManager}.
     */
    public static CommandManager getCommandManager() {
        return instance.commandManager;
    }
}
