package com.jesus_crie.modularbot.sharding;

import com.neovisionaries.ws.client.WebSocketFactory;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.audio.factory.IAudioSendFactory;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.IEventManager;
import net.dv8tion.jda.core.managers.impl.PresenceImpl;
import okhttp3.OkHttpClient;

import javax.security.auth.login.LoginException;

/**
 * Copy paste from super but with {@link com.jesus_crie.modularbot.sharding.ModularJDABuilder ModularJDABuilder} instead of {@link net.dv8tion.jda.core.JDABuilder JDABuilder}
 * and {@link com.jesus_crie.modularbot.sharding.ModularShard ModularShard} instead of {@link net.dv8tion.jda.core.entities.impl.JDAImpl JDAImpl}.
 */
public class ModularJDABuilder extends JDABuilder {

    public ModularJDABuilder(AccountType accountType) {
        super(accountType);
    }

    @Override
    public ModularJDABuilder setToken(String token) {
        super.setToken(token);
        return this;
    }

    @Override
    public ModularJDABuilder setHttpClientBuilder(OkHttpClient.Builder builder) {
        super.setHttpClientBuilder(builder);
        return this;
    }

    @Override
    public ModularJDABuilder setWebsocketFactory(WebSocketFactory factory) {
        super.setWebsocketFactory(factory);
        return this;
    }

    @Override
    public ModularJDABuilder setCorePoolSize(int size) {
        super.setCorePoolSize(size);
        return this;
    }

    @Override
    public ModularJDABuilder setAudioEnabled(boolean enabled) {
        super.setAudioEnabled(enabled);
        return this;
    }

    @Override
    public ModularJDABuilder setBulkDeleteSplittingEnabled(boolean enabled) {
        super.setBulkDeleteSplittingEnabled(enabled);
        return this;
    }

    @Override
    public ModularJDABuilder setEnableShutdownHook(boolean enable) {
        super.setEnableShutdownHook(enable);
        return this;
    }

    @Override
    public ModularJDABuilder setAutoReconnect(boolean autoReconnect) {
        super.setAutoReconnect(autoReconnect);
        return this;
    }

    @Override
    public ModularJDABuilder setEventManager(IEventManager manager) {
        super.setEventManager(manager);
        return this;
    }

    @Override
    public ModularJDABuilder setAudioSendFactory(IAudioSendFactory factory) {
        super.setAudioSendFactory(factory);
        return this;
    }

    @Override
    public ModularJDABuilder setIdle(boolean idle) {
        super.setIdle(idle);
        return this;
    }

    @Override
    public ModularJDABuilder setGame(Game game) {
        super.setGame(game);
        return this;
    }

    @Override
    public ModularJDABuilder setStatus(OnlineStatus status) {
        super.setStatus(status);
        return this;
    }

    @Override
    public ModularJDABuilder addEventListener(Object... listeners) {
        super.addEventListener(listeners);
        return this;
    }

    @Override
    public ModularJDABuilder removeEventListener(Object... listeners) {
        super.removeEventListener(listeners);
        return this;
    }

    @Override
    public ModularJDABuilder setMaxReconnectDelay(int maxReconnectDelay) {
        super.setMaxReconnectDelay(maxReconnectDelay);
        return this;
    }

    @Override
    public ModularJDABuilder useSharding(int shardId, int shardTotal) {
        super.useSharding(shardId, shardTotal);
        return this;
    }

    @Override
    public ModularShard buildAsync() throws LoginException, IllegalArgumentException, RateLimitedException {
        OkHttpClient.Builder httpClientBuilder = this.httpClientBuilder == null ? new OkHttpClient.Builder() : this.httpClientBuilder;
        WebSocketFactory wsFactory = this.wsFactory == null ? new WebSocketFactory() : this.wsFactory;
        ModularShard shard = new ModularShard(accountType, httpClientBuilder, wsFactory, autoReconnect, enableVoice, enableShutdownHook,
                enableBulkDeleteSplitting, corePoolSize, maxReconnectDelay);

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
        shard.login(token, shardInfo);
        return shard;
    }

    @Override
    public ModularShard buildBlocking() throws LoginException, IllegalArgumentException, InterruptedException, RateLimitedException {
        ModularShard shard = buildAsync();
        while(shard.getStatus() != JDA.Status.CONNECTED)
        {
            Thread.sleep(50);
        }
        return shard;
    }
}
