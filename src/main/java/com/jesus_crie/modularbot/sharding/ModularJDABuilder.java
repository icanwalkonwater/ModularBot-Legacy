package com.jesus_crie.modularbot.sharding;

import com.neovisionaries.ws.client.WebSocketFactory;
import net.dv8tion.jda.core.*;
import net.dv8tion.jda.core.audio.factory.IAudioSendFactory;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.hooks.IEventManager;
import net.dv8tion.jda.core.managers.impl.PresenceImpl;
import net.dv8tion.jda.core.requests.SessionReconnectQueue;
import okhttp3.OkHttpClient;

import javax.security.auth.login.LoginException;

/**
 * Copy paste from super but with {@link ModularJDABuilder} instead of {@link JDABuilder}
 * and {@link ModularShard} instead of {@link net.dv8tion.jda.core.entities.impl.JDAImpl}.
 */
@Deprecated
public class ModularJDABuilder extends JDABuilder {

    /**
     * @see JDABuilder#JDABuilder(AccountType)
     */
    public ModularJDABuilder(AccountType accountType) {
        super(accountType);
    }

    /**
     * @see JDABuilder#setToken(String)
     */
    @Override
    public ModularJDABuilder setToken(String token) {
        super.setToken(token);
        return this;
    }

    /**
     * @see JDABuilder#setHttpClientBuilder(OkHttpClient.Builder)
     */
    @Override
    public ModularJDABuilder setHttpClientBuilder(OkHttpClient.Builder builder) {
        super.setHttpClientBuilder(builder);
        return this;
    }

    /**
     * @see JDABuilder#setWebsocketFactory(WebSocketFactory)
     */
    @Override
    public ModularJDABuilder setWebsocketFactory(WebSocketFactory factory) {
        super.setWebsocketFactory(factory);
        return this;
    }

    /**
     * @see JDABuilder#setCorePoolSize(int)
     */
    @Override
    public ModularJDABuilder setCorePoolSize(int size) {
        super.setCorePoolSize(size);
        return this;
    }

    /**
     * @see JDABuilder#setAudioEnabled(boolean)
     */
    @Override
    public ModularJDABuilder setAudioEnabled(boolean enabled) {
        super.setAudioEnabled(enabled);
        return this;
    }

    /**
     * @see JDABuilder#setBulkDeleteSplittingEnabled(boolean)
     */
    @Override
    public ModularJDABuilder setBulkDeleteSplittingEnabled(boolean enabled) {
        super.setBulkDeleteSplittingEnabled(enabled);
        return this;
    }

    /**
     * @see JDABuilder#setEnableShutdownHook(boolean)
     */
    @Override
    public ModularJDABuilder setEnableShutdownHook(boolean enable) {
        super.setEnableShutdownHook(enable);
        return this;
    }

    /**
     * @see JDABuilder#setAutoReconnect(boolean)
     */
    @Override
    public ModularJDABuilder setAutoReconnect(boolean autoReconnect) {
        super.setAutoReconnect(autoReconnect);
        return this;
    }

    /**
     * @see JDABuilder#setEventManager(IEventManager)
     */
    @Override
    public ModularJDABuilder setEventManager(IEventManager manager) {
        super.setEventManager(manager);
        return this;
    }

    /**
     * @see JDABuilder#setAudioSendFactory(IAudioSendFactory)
     */
    @Override
    public ModularJDABuilder setAudioSendFactory(IAudioSendFactory factory) {
        super.setAudioSendFactory(factory);
        return this;
    }

    /**
     * @see JDABuilder#setIdle(boolean)
     */
    @Override
    public ModularJDABuilder setIdle(boolean idle) {
        super.setIdle(idle);
        return this;
    }

    /**
     * @see JDABuilder#setGame(Game)
     */
    @Override
    public ModularJDABuilder setGame(Game game) {
        super.setGame(game);
        return this;
    }

    /**
     * @see JDABuilder#setStatus(OnlineStatus)
     */
    @Override
    public ModularJDABuilder setStatus(OnlineStatus status) {
        super.setStatus(status);
        return this;
    }

    /**
     * @see JDABuilder#addEventListener(Object...)
     */
    @Override
    public ModularJDABuilder addEventListener(Object... listeners) {
        super.addEventListener(listeners);
        return this;
    }

    /**
     * @see JDABuilder#removeEventListener(Object...)
     */
    @Override
    public ModularJDABuilder removeEventListener(Object... listeners) {
        super.removeEventListener(listeners);
        return this;
    }

    /**
     * @see JDABuilder#setMaxReconnectDelay(int)
     */
    @Override
    public ModularJDABuilder setMaxReconnectDelay(int maxReconnectDelay) {
        super.setMaxReconnectDelay(maxReconnectDelay);
        return this;
    }

    /**
     * @see JDABuilder#setReconnectQueue(SessionReconnectQueue)
     */
    @Override
    public ModularJDABuilder setReconnectQueue(SessionReconnectQueue queue) {
        super.setReconnectQueue(queue);
        return this;
    }

    /**
     * @see JDABuilder#setShardedRateLimiter(ShardedRateLimiter)
     */
    @Override
    public ModularJDABuilder setShardedRateLimiter(ShardedRateLimiter rateLimiter) {
        super.setShardedRateLimiter(rateLimiter);
        return this;
    }

    /**
     * @see JDABuilder#setRequestTimeoutRetry(boolean)
     */
    @Override
    public ModularJDABuilder setRequestTimeoutRetry(boolean retryOnTimeout) {
        super.setRequestTimeoutRetry(retryOnTimeout);
        return this;
    }

    /**
     * @see JDABuilder#useSharding(int, int)
     */
    @Override
    public ModularJDABuilder useSharding(int shardId, int shardTotal) {
        super.useSharding(shardId, shardTotal);
        return this;
    }

    /**
     * @see JDABuilder#buildAsync()
     */
    @Override
    public ModularShard buildAsync() throws LoginException, IllegalArgumentException {
        OkHttpClient.Builder httpClientBuilder = this.httpClientBuilder == null ? new OkHttpClient.Builder() : this.httpClientBuilder;
        WebSocketFactory wsFactory = this.wsFactory == null ? new WebSocketFactory() : this.wsFactory;
        ModularShard shard = null/*new ModularShard(accountType, httpClientBuilder, wsFactory, shardRateLimiter, autoReconnect, enableVoice, enableShutdownHook,
                enableBulkDeleteSplitting, corePoolSize, maxReconnectDelay)*/;

        if (eventManager != null)
            shard.setEventManager(eventManager);

        if (audioSendFactory != null)
            shard.setAudioSendFactory(audioSendFactory);

        listeners.forEach(shard::addEventListener);
        shard.setStatus(JDA.Status.INITIALIZED);

        ((PresenceImpl) shard.getPresence())
                .setCacheGame(game)
                .setCacheIdle(idle)
                .setCacheStatus(status);

        try {
            shard.login(token, shardInfo, reconnectQueue);
        } catch (Exception ignore) {}
        return shard;
    }

    /**
     * @see JDABuilder#buildBlocking()
     */
    @Override
    public ModularShard buildBlocking() throws LoginException, IllegalArgumentException, InterruptedException {
        ModularShard shard = buildAsync();
        while(shard.getStatus() != JDA.Status.CONNECTED)
        {
            Thread.sleep(50);
        }
        return shard;
    }
}
