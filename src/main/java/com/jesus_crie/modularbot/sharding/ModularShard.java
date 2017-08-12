package com.jesus_crie.modularbot.sharding;

import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.config.ConfigHandler;
import com.jesus_crie.modularbot.utils.ModularThreadFactory;
import com.neovisionaries.ws.client.WebSocketFactory;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.EventListener;
import okhttp3.OkHttpClient;

import javax.security.auth.login.LoginException;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.jesus_crie.modularbot.utils.F.f;

public class ModularShard extends JDAImpl implements Comparable<ModularShard> {

    private boolean isReady;
    private final ModularShardInfos sInfos;
    private final ThreadPoolExecutor commandPool;

    /**
     * Package-Private constructor inherited from {@link JDAImpl}.
     * @see JDAImpl#JDAImpl(AccountType, OkHttpClient.Builder, WebSocketFactory, boolean, boolean, boolean, boolean, int, int)
     */
    ModularShard(AccountType accountType, OkHttpClient.Builder httpClientBuilder, WebSocketFactory wsFactory, boolean autoReconnect, boolean audioEnabled, boolean useShutdownHook, boolean bulkDeleteSplittingEnabled, int corePoolSize, int maxReconnectDelay) {
        super(accountType, httpClientBuilder, wsFactory, autoReconnect, audioEnabled, useShutdownHook, bulkDeleteSplittingEnabled, corePoolSize, maxReconnectDelay);
        sInfos = new ModularShardInfos();
        commandPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }

    /**
     * @see JDAImpl#login(String, ShardInfo)
     */
    @Override
    public void login(String token, ShardInfo shardInfo) throws LoginException, RateLimitedException {
        final EventListener readyListener = event -> {
            if (event instanceof ReadyEvent) {
                ModularBot.LOGGER().info("Start", f("Shard %s is ready !", sInfos.getShardString()));
                isReady = true;
                removeEventListener(this);
            }
        };
        addEventListener(readyListener);

        super.login(token, shardInfo);
        pool.setThreadFactory(new ModularThreadFactory(this, "Main", true));
        commandPool.setThreadFactory(new ModularThreadFactory(this, "Command", true));
    }

    /**
     * @see JDAImpl#getIdentifierString()
     */
    @Override
    public String getIdentifierString() {
        if (shardInfo != null)
            return f("%s %s", ModularBot.instance().getConfig().getAppName(), sInfos.getShardString());
        else
            return ModularBot.instance().getConfig().getAppName();
    }

    /**
     * @see JDAImpl#shutdown()
     */
    @Override
    public void shutdown() {
        ModularBot.LOGGER().info("Stop", f("Shutting down shard %s", sInfos.getShardString()));
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

    @Override
    public ShardInfo getShardInfo() {
        return super.getShardInfo();
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
        Objects.requireNonNull(obj);
        return obj instanceof ModularShard && ((ModularShard) obj).shardInfo.getShardId() == shardInfo.getShardId();
    }

    /**
     * Compare shards with there {@link ShardInfo#getShardId()}.
     * Used to sort shard list.
     * @param shard the shard that will be compared to.
     * @return 0 if equals, 1 if <b>this</b> as an higher id than the other shard.
     *      -1 if this shard has an id inferior than the other shard.
     */
    @Override
    public int compareTo(final ModularShard shard) {
        Objects.requireNonNull(shard);
        if (equals(shard))
            return 0;
        return shardInfo.getShardId() < shard.shardInfo.getShardId() ? -1 : 1;
    }

    @Override
    public String toString() {
        ConfigHandler c = ModularBot.instance().getConfig();
        return f("%s v%s Shard %s #%s", c.getAppName(), c.getVersion().toString(), sInfos.getShardString(), hashCode());
    }
}
