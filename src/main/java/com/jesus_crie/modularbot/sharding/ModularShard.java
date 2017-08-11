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

    public ModularShard(AccountType accountType, OkHttpClient.Builder httpClientBuilder, WebSocketFactory wsFactory, boolean autoReconnect, boolean audioEnabled, boolean useShutdownHook, boolean bulkDeleteSplittingEnabled, int corePoolSize, int maxReconnectDelay) {
        super(accountType, httpClientBuilder, wsFactory, autoReconnect, audioEnabled, useShutdownHook, bulkDeleteSplittingEnabled, corePoolSize, maxReconnectDelay);
        commandPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        mightyPool = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(0);
    }

    @Override
    public void login(String token, ShardInfo shardInfo) throws LoginException, RateLimitedException {
        super.login(token, shardInfo);
        pool.setThreadFactory(new ModularThreadFactory(this, "Main", true));
        commandPool.setThreadFactory(new ModularThreadFactory(this, "Command", true));
        mightyPool.setThreadFactory(new ModularThreadFactory(this, "Mighty", false));
    }

    @Override
    public String getIdentifierString() {
        if (shardInfo != null)
            return f("%s %s/%s", ModularBot.instance().getConfig().getAppName(), shardInfo.getShardId(), shardInfo.getShardTotal());
        else
            return ModularBot.instance().getConfig().getAppName();
    }

    @Override
    public boolean equals(Object obj) {
        Objects.requireNonNull(obj);
        return obj instanceof ModularShard && ((ModularShard) obj).shardInfo.getShardId() == shardInfo.getShardId();
    }

    @Override
    public int compareTo(final ModularShard obj) {
        Objects.requireNonNull(obj);
        if (equals(obj))
            return 0;
        return shardInfo.getShardId() < obj.shardInfo.getShardId() ? -1 : 1;
    }
}
