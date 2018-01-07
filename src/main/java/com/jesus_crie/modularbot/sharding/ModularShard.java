package com.jesus_crie.modularbot.sharding;

import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.config.IConfigHandler;
import com.jesus_crie.modularbot.listener.CommandListener;
import com.jesus_crie.modularbot.listener.DecoratorDeleteListener;
import com.jesus_crie.modularbot.listener.ReadyListener;
import com.jesus_crie.modularbot.utils.ModularThreadFactory;
import com.neovisionaries.ws.client.WebSocketFactory;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.requests.SessionReconnectQueue;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.SessionController;
import okhttp3.OkHttpClient;

import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;
import java.util.concurrent.*;

import static com.jesus_crie.modularbot.utils.F.f;

@Deprecated
public class ModularShard extends JDAImpl implements Comparable<ModularShard> {

    private boolean isReady;
    private final ModularShardInfos sInfos;
    private final ThreadPoolExecutor commandPool;

    /**
     * @see JDAImpl#JDAImpl(AccountType, String, SessionController, OkHttpClient.Builder, WebSocketFactory, boolean, boolean, boolean, boolean, boolean, boolean, int, int, ConcurrentMap)
     */
    public ModularShard(AccountType accountType, String token, SessionController controller, OkHttpClient.Builder httpClientBuilder, WebSocketFactory wsFactory, boolean autoReconnect, boolean audioEnabled, boolean useShutdownHook, boolean bulkDeleteSplittingEnabled, boolean retryOnTimeout, boolean enableMDC, int corePoolSize, int maxReconnectDelay, ConcurrentMap<String, String> contextMap) {
        super(accountType, token, controller, httpClientBuilder, wsFactory, autoReconnect, audioEnabled, useShutdownHook, bulkDeleteSplittingEnabled, retryOnTimeout, enableMDC, corePoolSize, maxReconnectDelay, contextMap);
        sInfos = new ModularShardInfos();
        commandPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }

    public void login(String token, ShardInfo shardInfo, SessionReconnectQueue reconnectQueue) throws LoginException, RateLimitedException {
        // Ready listener
        ReadyListener listener = new ReadyListener();
        addEventListener(listener);

        //super.login(token, shardInfo, reconnectQueue);
        listener.get();
        ModularBot.logger().info("Start", f("Shard %s is ready !", sInfos.getShardString()));
        isReady = true;
        removeEventListener(listener);

        pool.setThreadFactory(new ModularThreadFactory(this, "Main"));
        commandPool.setThreadFactory(new ModularThreadFactory(this, "Command"));

        // Listeners
        addEventListener(new CommandListener());
        addEventListener(new DecoratorDeleteListener());
    }

    /**
     * @see JDAImpl#getIdentifierString()
     */
    @Override
    @Nullable
    public String getIdentifierString() {
        if (shardInfo != null)
            return f("%s %s", ModularBot.getConfig().getAppName(), sInfos.getShardString());
        else
            return ModularBot.getConfig().getAppName();
    }

    /**
     * Get an user by his name and his discriminator.
     * The user must be on this shard.
     * @param name the name of the user (not the nick)
     * @param discriminator the discriminator of the user
     * @return a possibly-null {@link User}.
     */
    public User getUserByNameAndDiscriminator(String name, String discriminator) {
        Checks.notEmpty(name, "name");
        Checks.notEmpty(discriminator, "discriminator");
        return getUsersByName(name, true).stream()
                .filter(u -> u.getDiscriminator().equals(discriminator))
                .findAny()
                .orElse(null);
    }

    /**
     * @see JDAImpl#shutdown()
     */
    @Override
    public void shutdown() {
        ModularBot.logger().info("Stop", f("Shutting down shard %s", sInfos.getShardString()));

        try {
            commandPool.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException ignore) {
        } finally {
            commandPool.shutdown();
            super.shutdown();
            pool.shutdown();
        }
    }

    /**
     * @see JDAImpl#shutdownNow()
     */
    @Override
    public void shutdownNow() {
        commandPool.shutdownNow();
        super.shutdownNow();
    }

    /**
     * Used to check if the {@link net.dv8tion.jda.core.events.ReadyEvent} has been
     * received by this shard.
     * @return true if the shard is ready.
     */
    public boolean isReady() {
        return isReady;
    }

    /**
     * Get the {@link java.util.concurrent.ExecutorService} use to run commands.
     * @return a {@link ThreadPoolExecutor} used to run commands.
     */
    public ThreadPoolExecutor getCommandPool() {
        return commandPool;
    }

    /**
     * Get the main {@link java.util.concurrent.ScheduledExecutorService} of the shard.
     * Used by JDA.
     * @return the scheduled executor of the shard.
     */
    public ScheduledThreadPoolExecutor getGeneralPool() {
        return pool;
    }

    public class ModularShardInfos {

        public String getShardString() {
            return f("%d/%d", shardInfo.getShardId() + 1, shardInfo.getShardTotal());
        }
    }

    /**
     * Used to compare shards using {@link ShardInfo#getShardId()}
     * @param obj the object that will be compared.
     * @return true if the {@link ShardInfo#getShardId()} is the same.
     */
    @Override
    public boolean equals(Object obj) {
        Checks.notNull(obj, "obj");
        return obj instanceof ModularShard && ((ModularShard) obj).shardInfo.getShardId() == shardInfo.getShardId();
    }

    /**
     * Compare shards with there {@link ShardInfo#getShardId()}.
     * Used to sort shard list.
     * @param shard the shard that will be compared to.
     * @return 0 if equals, 1 if <b>this</b> as an higher id than the other shard.
     *      -1 if this shard has an id inferior than the other shard.
     */
    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(final ModularShard shard) {
        Checks.notNull(shard, "shard");

        if (equals(shard))
            return 0;
        return shardInfo.getShardId() < shard.shardInfo.getShardId() ? -1 : 1;
    }

    @Override
    public String toString() {
        IConfigHandler c = ModularBot.getConfig();
        return f("%s v%s Shard %s #%s", c.getAppName(), c.getVersion().toString(), sInfos.getShardString(), hashCode());
    }
}
