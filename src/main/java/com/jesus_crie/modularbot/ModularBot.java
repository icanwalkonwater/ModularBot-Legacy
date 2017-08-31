package com.jesus_crie.modularbot;

import com.jesus_crie.modularbot.config.ConfigHandler;
import com.jesus_crie.modularbot.listener.ModularCommandListener;
import com.jesus_crie.modularbot.log.JDALogger;
import com.jesus_crie.modularbot.log.LogHandler;
import com.jesus_crie.modularbot.manager.CommandManager;
import com.jesus_crie.modularbot.manager.ModularEventManager;
import com.jesus_crie.modularbot.sharding.ModularJDABuilder;
import com.jesus_crie.modularbot.sharding.ModularShard;
import com.jesus_crie.modularbot.stats.Stats;
import com.jesus_crie.modularbot.utils.MiscUtils;
import com.jesus_crie.modularbot.utils.Status;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.SimpleLog;
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
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.jesus_crie.modularbot.utils.F.f;

/**
 * Can only be instantiated one time.
 */
public class ModularBot {

    private static ModularBot instance;
    private static LogHandler logger;
    private static boolean isReady = false;
    private static boolean useAudio;
    private static ModularJDABuilder builderCache = null;

    private final String token;
    private int maxShard;
    private final Game readyStatus;

    private final ConfigHandler config;
    private final CommandManager commandManager;
    private final List<ModularShard> shards;

    private final ScheduledThreadPoolExecutor mightyPool;

    /**
     * Package-Private builder.
     * @param token the token provided.
     * @param config a custom {@link ConfigHandler}.
     * @param logger a custom {@link LogHandler}.
     * @param command a custom {@link ModularCommandListener}.
     * @param useAudio if the audio must be enabled.
     * @param readyStatus the status to be displayed when the bot is fully operational.
     */
    ModularBot(String token, ConfigHandler config, LogHandler logger, ModularCommandListener command, boolean useAudio, Game readyStatus) {
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
        Checks.notNull(readyStatus, "readyStatus");

        this.readyStatus = readyStatus;
        commandManager = new CommandManager(command);

        instance = this;
        shards = new ArrayList<>();
        ModularBot.logger = logger;
        SimpleLog.LEVEL = SimpleLog.Level.OFF;
        SimpleLog.addListener(new JDALogger());
        logger.info("Start", "LogHandler initialized !");

        this.token = token;
        this.config = config;
        ModularBot.useAudio = useAudio;

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
        logger.info("Start", "Reset stats");
        Stats.reset();
        logger.info("Start", "Attempting to spawn and start shards...");
        restartShards();

        logger.info("Start", "Enabling auto save...");
        config.startAutoSave();

        logger.info("Start", "Ready !");
        isReady = true;
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

        initShardBuilder();

        for (int i = 0; i < maxShard; i++) {
            logger.info("Start", f("Starting shard %s of %s...", i + 1, maxShard));
            shards.add(builderCache.useSharding(i, maxShard).buildAsync());
            try {
                Thread.sleep(5000);
            } catch (Exception ignore) {}
        }

        shards.sort(ModularShard::compareTo);

        dispatchCommand(s -> s.getPresence().setGame(readyStatus));
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

        shards.set(shardId, builderCache.useSharding(shardId, maxShard).buildBlocking());

        shards.sort(ModularShard::compareTo);
    }

    /**
     * Create an instance of {@link ModularJDABuilder} if not existing.
     */
    protected void initShardBuilder() {
        if (builderCache == null) {
            builderCache = new ModularJDABuilder(AccountType.BOT)
                    .setToken(token)
                    .setAutoReconnect(true)
                    .setAudioEnabled(useAudio)
                    .setEventManager(new ModularEventManager())
                    .setIdle(false)
                    .setGame(Status.STARTING);
        }
    }

    /**
     * Dispatch some code through all shards.
     * @param action the action to perform for each shard.
     */
    public void dispatchCommand(Consumer<ModularShard> action) {
        shards.forEach(action);
    }

    /**
     * Shutdown every shards one by one.
     * @param force if true {@link ModularShard#shutdownNow()} will be used instead of {@link ModularShard#shutdown()}
     */
    private void shutdownShards(boolean force) {
        dispatchCommand(s -> s.getPresence().setGame(Status.STOPPING));
        if (force) {
            dispatchCommand(ModularShard::shutdown);
        } else {
            dispatchCommand(ModularShard::shutdownNow);
        }
    }

    /**
     * Shutdown all shards, save the config and stop thread pools.
     * @param force if true, shutdown as fast as is possible.
     */
    public void shutdown(boolean force) {
        isReady = false;
        logger.info("Stop", f("Shutting down %s shards...", shards.size()));
        shutdownShards(force);

        logger.info("Stop", "Saving config...");
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
        instance = null;
    }

    /**
     * Register each listener in each shard of the bot.
     * @param listeners the listeners to register.
     * @see ModularShard#addEventListener(Object...)
     */
    public void addEventListener(EventListener... listeners) {
        dispatchCommand(s -> s.addEventListener((Object[]) listeners));
    }

    /**
     * Unregister each listener in each shard of the bot.
     * @param listeners the listeners to unregister.
     * @see ModularShard#removeEventListener(Object...)
     */
    public void removeEventListener(EventListener... listeners) {
        dispatchCommand(s -> s.removeEventListener((Object[]) listeners));
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
     * Get the shard that handle the given guild.
     * @param id the id of the guild.
     * @return the {@link ModularShard} that handle this guild.
     */
    public ModularShard getShardForGuildId(long id) {
        return getShardById((int) (id >> 22) % shards.size());
    }

    /**
     * Get the shard that will receive private messages.
     * @return the shard 0.
     */
    public ModularShard getDMShard() {
        return shards.get(0);
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
     * Used to collect infos across shards and collect them into one list.
     * Mainly used for stats.
     * @param action used to collect data in each shards and convert them into {@link T}.
     * @param filter used to determinate if an entry can be merged with the others. The 1st argument is the object
     *               and the 2nd is a list containing the current state of the merging.
     * @param <T> the output will be a list of this type.
     * @return a list of {@link T} containing merged data from all shards.
     */
    public <T> List<T> collectCumulativeShardInfos(Function<ModularShard, T> action, BiPredicate<T, List<T>> filter) {
        List<T> datas = new ArrayList<>();
        shards.stream()
                .map(action)
                .forEach(t -> {
                    if (filter.test(t, datas))
                        datas.add(t);
                });
        return datas;
    }

    /**
     * Query the recommended amount of shards from the discord API.
     * @return the recommended amount of shards.
     */
    @SuppressWarnings("ConstantConditions")
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
        } catch (Exception e) {
            logger.fatal("Start", e.getClass().getSimpleName() + " while getting the recommended amount of shards ! Using 1 shard.");
            if (e.getMessage() != null)
                logger.fatal("Start", e.getMessage());
            return 1;
        }
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
     * @return the instance of {@link ModularBot}.
     */
    public static ModularBot instance() {
        if (instance == null)
            throw new IllegalStateException("ModularBot has not been initialized yet !");
        return instance;
    }

    /**
     * Check if the bot is connected to discord and all of the components can be used.
     * @return true if everything can be used, otherwise false.
     */
    public static boolean isReady() {
        return isReady;
    }

    /**
     * Check if the audio is enabled in this instance of ModularBot.
     * @return true if the audio is enabled, otherwise false.
     */
    public static boolean isAudioEnabled() {
        return useAudio;
    }

    /**
     * Get the current implementation of the {@link LogHandler} that is used by the application.
     * @return an implementation of {@link LogHandler}.
     */
    public static LogHandler logger() {
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
