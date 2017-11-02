package com.jesus_crie.modularbot;

import com.jesus_crie.modularbot.config.ConfigHandler;
import com.jesus_crie.modularbot.listener.ModularCommandListener;
import com.jesus_crie.modularbot.log.JDALogger;
import com.jesus_crie.modularbot.log.LogHandler;
import com.jesus_crie.modularbot.manager.CommandManager;
import com.jesus_crie.modularbot.manager.MessageDecoratorManager;
import com.jesus_crie.modularbot.manager.ModularEventManager;
import com.jesus_crie.modularbot.sharding.ModularJDABuilder;
import com.jesus_crie.modularbot.sharding.ModularShard;
import com.jesus_crie.modularbot.stats.Stats;
import com.jesus_crie.modularbot.utils.MiscUtils;
import net.dv8tion.jda.bot.JDABot;
import net.dv8tion.jda.client.JDAClient;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.IEventManager;
import net.dv8tion.jda.core.managers.AudioManager;
import net.dv8tion.jda.core.managers.Presence;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.restaction.GuildAction;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.SimpleLog;
import net.dv8tion.jda.core.utils.cache.CacheView;
import net.dv8tion.jda.core.utils.cache.SnowflakeCacheView;
import net.dv8tion.jda.webhook.WebhookClient;
import net.dv8tion.jda.webhook.WebhookCluster;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.jesus_crie.modularbot.utils.F.f;

/**
 * Can only be instantiated one time.
 */
public class ModularBot implements JDA {

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

    private final WebhookCluster webhookCluster;
    private final ScheduledThreadPoolExecutor webhookPool;

    private final MessageDecoratorManager decoratorManager;

    private final ScheduledThreadPoolExecutor mightyPool;

    /**
     * Package-Private builder.
     * @param token the token provided.
     * @param config a custom {@link ConfigHandler}.
     * @param logger a custom {@link LogHandler}.
     * @param command a custom {@link ModularCommandListener}.
     * @param readyStatus the status to be displayed when the bot is fully operational.
     * @param useAudio if the audio must be enabled.
     * @param cacheDismissible if the dismissible decorators can be cached.
     * @param useWebhook if you want to use the webhooks in your application.
     */
    ModularBot(String token, ConfigHandler config, LogHandler logger, ModularCommandListener command, Game readyStatus, boolean useAudio, boolean cacheDismissible, boolean useWebhook) {
        Thread.currentThread().setName(f("%s Main", config.getAppName(), Thread.currentThread().getId()));

        mightyPool = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(0, r -> {
            Thread t = new Thread(r);
            t.setName(f("%s Mighty #%s", config.getAppName(), t.getId()));
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

        if (useWebhook) {
            webhookPool = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(0, r -> {
                Thread t = new Thread(r);
                t.setName(f("%s Webhook #%s", config.getAppName(), t.getId()));
                return t;
            });
            webhookCluster = new WebhookCluster().setDefaultExecutorService(webhookPool);
        } else {
            webhookPool = null;
            webhookCluster = null;
        }

        decoratorManager = new MessageDecoratorManager(cacheDismissible);
    }

    /**
     * Use to create shards and connect to discord.
     * @throws LoginException if the token is wrong.
     * @throws RateLimitedException if we are rate-limited.
     */
    public void connectToDiscord() throws LoginException, RateLimitedException {
        logger.info("Start", "Reset stats");
        Stats.reset();
        logger.info("Start", "Attempting to spawn and start shards...");
        restartShards();

        if (webhookCluster != null) webhookCluster.setDefaultExecutorService(mightyPool);

        logger.info("Start", "Enabling auto save...");
        config.startAutoSave();

        logger.info("Start", "Resuming decorators...");
        decoratorManager.getCache().loadAndResumeCache();

        logger.info("Start", "Ready !");
        isReady = true;
    }

    /**
     * Used to regenerate every shard of the bot.
     * @throws LoginException if the token is wrong.
     * @throws RateLimitedException if we are rate-limited.
     */
    public void restartShards() throws LoginException, RateLimitedException {
        if (!shards.isEmpty()) {
            shards.forEach(s -> {
                s.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, com.jesus_crie.modularbot.utils.Status.STOPPING);
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
                if (i != maxShard - 1)
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
                    .setGame(com.jesus_crie.modularbot.utils.Status.STARTING);
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
     * Create a {@link WebhookClient} from a webhook using the webhook pool and the dedicated cluster.
     * @param webhook the base webhook.
     * @return a new client for the given webhook.
     */
    public WebhookClient createWebHookClient(Webhook webhook) {
        if (webhookCluster == null) throw new UnsupportedOperationException("Webhooks are disabled !");
        return webhookCluster.newBuilder(webhook).build();
    }

    /**
     * Shutdown every shards one by one.
     * @param force if true {@link ModularShard#shutdownNow()} will be used instead of {@link ModularShard#shutdown()}
     */
    private void shutdownShards(boolean force) {
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
        dispatchCommand(s -> s.getPresence().setGame(com.jesus_crie.modularbot.utils.Status.STOPPING));

        logger.info("Stop", "Saving cached decorators...");
        decoratorManager.getCache().saveCache();

        logger.info("Stop", "Destroying decorators...");
        if (decoratorManager.size() > 20) decoratorManager.destroyAllAsync(mightyPool, decoratorManager.size() / 10);
        else decoratorManager.destroyAll();

        logger.info("Stop", "Closing webhooks...");
        if (webhookCluster != null) webhookCluster.close();

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
    public ModularShard getFirstShard() {
        return shards.get(0);
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
            logger.warning("Start", e.getClass().getSimpleName() + " while getting the recommended amount of shards ! Using 1 shard.");
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

    @Override
    public SelfUser getSelfUser() {
        return getFirstShard().getSelfUser();
    }

    @Override
    public Presence getPresence() {
        return getFirstShard().getPresence();
    }

    @Override
    public ShardInfo getShardInfo() {
        return getFirstShard().getShardInfo();
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public long getResponseTotal() {
        return getFirstShard().getResponseTotal();
    }

    @Override
    public int getMaxReconnectDelay() {
        return getFirstShard().getMaxReconnectDelay();
    }

    @Override
    public void setAutoReconnect(boolean reconnect) {
        dispatchCommand(s -> setAutoReconnect(reconnect));
    }

    @Override
    public void setRequestTimeoutRetry(boolean retryOnTimeout) {
        dispatchCommand(s -> setRequestTimeoutRetry(retryOnTimeout));
    }

    @Override
    public boolean isAutoReconnect() {
        return getFirstShard().isAutoReconnect();
    }

    @Override
    public boolean isAudioEnabled() {
        return useAudio;
    }

    @Override
    public boolean isBulkDeleteSplittingEnabled() {
        return getFirstShard().isBulkDeleteSplittingEnabled();
    }

    @Override
    public void shutdown() {
        shutdown(false);
    }

    @Override
    public void shutdownNow() {
        shutdown(true);
    }

    @Override
    public AccountType getAccountType() {
        return AccountType.BOT;
    }

    @Override
    public JDAClient asClient() {
        return getFirstShard().asClient();
    }

    @Override
    public JDABot asBot() {
        return getFirstShard().asBot();
    }

    @Override
    public Status getStatus() {
        return getFirstShard().getStatus();
    }

    @Override
    public long getPing() {
        return (long) shards.stream()
                .mapToLong(JDAImpl::getPing)
                .average()
                .orElse(getFirstShard().getPing());
    }

    @Override
    public List<String> getCloudflareRays() {
        return shards.stream()
                .flatMap(s -> s.getCloudflareRays().stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getWebSocketTrace() {
        return shards.stream()
                .flatMap(s -> s.getWebSocketTrace().stream())
                .collect(Collectors.toList());
    }

    @Override
    public void setEventManager(IEventManager manager) {
        dispatchCommand(s -> s.setEventManager(manager));
    }

    @Override
    public void addEventListener(Object... listeners) {
        dispatchCommand(s -> s.addEventListener(listeners));
    }

    @Override
    public void removeEventListener(Object... listeners) {
        dispatchCommand(s -> s.removeEventListener(listeners));
    }

    @Override
    public List<Object> getRegisteredListeners() {
        return shards.stream()
                .flatMap(s -> s.getRegisteredListeners().stream())
                .collect(Collectors.toList());
    }

    @Override
    public GuildAction createGuild(String name) {
        return getFirstShard().createGuild(name);
    }

    @Override
    public CacheView<AudioManager> getAudioManagerCache() {
        throw new NotImplementedException();
    }

    @Override
    public List<AudioManager> getAudioManagers() {
        return shards.stream()
                .flatMap(s -> s.getAudioManagers().stream())
                .collect(Collectors.toList());
    }

    @Override
    public SnowflakeCacheView<User> getUserCache() {
        throw new NotImplementedException();
    }

    @Override
    public List<User> getUsers() {
        return shards.stream()
                .flatMap(s -> s.getUsers().stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getUsersByName(String name, boolean ignoreCase) {
        return shards.stream()
                .flatMap(s -> s.getUsersByName(name, ignoreCase).stream())
                .collect(Collectors.toList());
    }

    @Override
    public User getUserById(long id) {
        return shards.stream()
                .map(s -> s.getUserById(id))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    public User getUserById(String id) {
        return shards.stream()
                .map(s -> s.getUserById(id))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    public RestAction<User> retrieveUserById(String id) {
        return getFirstShard().retrieveUserById(id);
    }

    @Override
    public RestAction<User> retrieveUserById(long id) {
        return getFirstShard().retrieveUserById(id);
    }

    @Override
    public List<Guild> getMutualGuilds(User... users) {
        return shards.stream()
                .flatMap(s -> s.getMutualGuilds(users).stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<Guild> getMutualGuilds(Collection<User> users) {
        return shards.stream()
                .flatMap(s -> s.getMutualGuilds(users).stream())
                .collect(Collectors.toList());
    }

    @Override
    public SnowflakeCacheView<Guild> getGuildCache() {
        throw new NotImplementedException();
    }

    @Override
    public List<Guild> getGuilds() {
        return shards.stream()
                .flatMap(s -> s.getGuilds().stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<Guild> getGuildsByName(String name, boolean ignoreCase) {
        return shards.stream()
                .flatMap(s -> s.getGuildsByName(name, ignoreCase).stream())
                .collect(Collectors.toList());
    }

    @Override
    public Guild getGuildById(String id) {
        try {
            return getGuildById(Long.parseLong(id));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public Guild getGuildById(long id) {
        return getShardForGuildId(id).getGuildById(id);
    }

    @Override
    public SnowflakeCacheView<Role> getRoleCache() {
        throw new NotImplementedException();
    }

    @Override
    public List<Role> getRoles() {
        return shards.stream()
                .flatMap(s -> s.getRoles().stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<Role> getRolesByName(String name, boolean ignoreCase) {
        return shards.stream()
                .flatMap(s -> s.getRolesByName(name, ignoreCase).stream())
                .collect(Collectors.toList());
    }

    @Override
    public Role getRoleById(long id) {
        return shards.stream()
                .map(s -> s.getRoleById(id))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    public Role getRoleById(String id) {
        return shards.stream()
                .map(s -> s.getRoleById(id))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    public SnowflakeCacheView<Category> getCategoryCache() {
        throw new NotImplementedException();
    }

    @Override
    public List<Category> getCategories() {
        return shards.stream()
                .flatMap(s -> s.getCategories().stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<Category> getCategoriesByName(String name, boolean ignoreCase) {
        return shards.stream()
                .flatMap(s -> s.getCategoriesByName(name, ignoreCase).stream())
                .collect(Collectors.toList());
    }

    @Override
    public Category getCategoryById(long id) {
        return shards.stream()
                .map(s -> s.getCategoryById(id))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    public Category getCategoryById(String id) {
        return shards.stream()
                .map(s -> s.getCategoryById(id))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    public SnowflakeCacheView<TextChannel> getTextChannelCache() {
        throw new NotImplementedException();
    }

    @Override
    public List<TextChannel> getTextChannels() {
        return shards.stream()
                .flatMap(s -> s.getTextChannels().stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<TextChannel> getTextChannelsByName(String name, boolean ignoreCase) {
        return shards.stream()
                .flatMap(s -> s.getTextChannelsByName(name, ignoreCase).stream())
                .collect(Collectors.toList());
    }

    @Override
    public TextChannel getTextChannelById(long id) {
        return shards.stream()
                .map(s -> s.getTextChannelById(id))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    public TextChannel getTextChannelById(String id) {
        return shards.stream()
                .map(s -> s.getTextChannelById(id))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    public SnowflakeCacheView<VoiceChannel> getVoiceChannelCache() {
        throw new NotImplementedException();
    }

    @Override
    public List<VoiceChannel> getVoiceChannels() {
        return shards.stream()
                .flatMap(s -> s.getVoiceChannels().stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<VoiceChannel> getVoiceChannelByName(String name, boolean ignoreCase) {
        return shards.stream()
                .flatMap(s -> s.getVoiceChannelByName(name, ignoreCase).stream())
                .collect(Collectors.toList());
    }

    @Override
    public VoiceChannel getVoiceChannelById(long id) {
        return shards.stream()
                .map(s -> s.getVoiceChannelById(id))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    public VoiceChannel getVoiceChannelById(String id) {
        return shards.stream()
                .map(s -> s.getVoiceChannelById(id))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    public SnowflakeCacheView<PrivateChannel> getPrivateChannelCache() {
        throw new NotImplementedException();
    }

    @Override
    public List<PrivateChannel> getPrivateChannels() {
        return shards.stream()
                .flatMap(s -> s.getPrivateChannels().stream())
                .collect(Collectors.toList());
    }

    @Override
    public PrivateChannel getPrivateChannelById(long id) {
        return shards.stream()
                .map(s -> s.getPrivateChannelById(id))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    public PrivateChannel getPrivateChannelById(String id) {
        return shards.stream()
                .map(s -> s.getPrivateChannelById(id))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    public SnowflakeCacheView<Emote> getEmoteCache() {
        throw new NotImplementedException();
    }

    @Override
    public List<Emote> getEmotes() {
        return shards.stream()
                .flatMap(s -> s.getEmotes().stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<Emote> getEmotesByName(String name, boolean ignoreCase) {
        return shards.stream()
                .flatMap(s -> s.getEmotesByName(name, ignoreCase).stream())
                .collect(Collectors.toList());
    }

    @Override
    public Emote getEmoteById(long id) {
        return shards.stream()
                .map(s -> s.getEmoteById(id))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    public Emote getEmoteById(String id) {
        return shards.stream()
                .map(s -> s.getEmoteById(id))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
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
    public static boolean useAudio() {
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

    /**
     * Get the decorator manager of the application.
     * Used internally.
     * @return the {@link MessageDecoratorManager}.
     */
    public static MessageDecoratorManager getDecoratorManager() {
        return instance.decoratorManager;
    }
}
