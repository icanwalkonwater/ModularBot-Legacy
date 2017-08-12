package com.jesus_crie.modularbot.sharding;

import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.utils.ModularThreadFactory;
import com.neovisionaries.ws.client.WebSocketFactory;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import okhttp3.OkHttpClient;

import javax.security.auth.login.LoginException;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

import static com.jesus_crie.modularbot.utils.F.f;

public class ModularShard extends JDAImpl implements Comparable<ModularShard> {

    private final ThreadPoolExecutor commandPool;
    private final ScheduledThreadPoolExecutor mightyPool;

    /**
     * Package-Private constructor inherited from {@link JDAImpl}.
     * @see JDAImpl#JDAImpl(AccountType, OkHttpClient.Builder, WebSocketFactory, boolean, boolean, boolean, boolean, int, int)
     */
    ModularShard(AccountType accountType, OkHttpClient.Builder httpClientBuilder, WebSocketFactory wsFactory, boolean autoReconnect, boolean audioEnabled, boolean useShutdownHook, boolean bulkDeleteSplittingEnabled, int corePoolSize, int maxReconnectDelay) {
        super(accountType, httpClientBuilder, wsFactory, autoReconnect, audioEnabled, useShutdownHook, bulkDeleteSplittingEnabled, corePoolSize, maxReconnectDelay);
        commandPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        mightyPool = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(0);
    }

    /**
     * @see JDAImpl#login(String, ShardInfo)
     */
    @Override
    public void login(String token, ShardInfo shardInfo) throws LoginException, RateLimitedException {
        super.login(token, shardInfo);
        pool.setThreadFactory(new ModularThreadFactory(this, "Main", true));
        commandPool.setThreadFactory(new ModularThreadFactory(this, "Command", true));
        mightyPool.setThreadFactory(new ModularThreadFactory(this, "Mighty", false));
    }

    /**
     * @see JDAImpl#getIdentifierString()
     */
    @Override
    public String getIdentifierString() {
        if (shardInfo != null)
            return f("%s %s/%s", ModularBot.instance().getConfig().getAppName(), shardInfo.getShardId(), shardInfo.getShardTotal());
        else
            return ModularBot.instance().getConfig().getAppName();
    }

    /**
     * Get the {@link java.util.concurrent.ExecutorService} use to run commands.
     * @return a {@link ThreadPoolExecutor} used to run commands.
     */
    public ThreadPoolExecutor getCommandPool() {
        return commandPool;
    }

    /**
     * An {@link java.util.concurrent.ExecutorService} that need to be used for important
     * action such as saving the config.
     * @return a {@link ScheduledThreadPoolExecutor} used for important tasks.
     */
    public ScheduledThreadPoolExecutor getMightyPool() {
        return mightyPool;
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
}
