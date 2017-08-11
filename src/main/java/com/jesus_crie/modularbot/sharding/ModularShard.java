package com.jesus_crie.modularbot.sharding;

import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.manager.ThreadManager;
import com.neovisionaries.ws.client.WebSocketFactory;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import okhttp3.OkHttpClient;

import javax.security.auth.login.LoginException;

public class ModularShard extends JDAImpl implements Comparable<ModularShard> {

    private ThreadManager threadManager;

    public ModularShard(AccountType accountType, OkHttpClient.Builder httpClientBuilder, WebSocketFactory wsFactory, boolean autoReconnect, boolean audioEnabled, boolean useShutdownHook, boolean bulkDeleteSplittingEnabled, int corePoolSize, int maxReconnectDelay) {
        super(accountType, httpClientBuilder, wsFactory, autoReconnect, audioEnabled, useShutdownHook, bulkDeleteSplittingEnabled, corePoolSize, maxReconnectDelay);
    }

    @Override
    public void login(String token, ShardInfo shardInfo) throws LoginException, RateLimitedException {
        super.login(token, shardInfo);
        threadManager = new ThreadManager(ModularBot.instance().getConfig().getAppName(), shardInfo.getShardId());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ModularShard && ((ModularShard) obj).shardInfo.getShardId() == shardInfo.getShardId();
    }

    @Override
    public int compareTo(ModularShard o) {
        if (equals(o))
            return 0;
        return shardInfo.getShardId() < o.shardInfo.getShardId() ? -1 : 1;
    }
}
