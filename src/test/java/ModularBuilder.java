import com.jesus_crie.modularbot.config.IConfigHandler;
import com.jesus_crie.modularbot.listener.ICommandHandler;
import com.jesus_crie.modularbot.utils.BiIntFunction;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.audio.factory.IAudioSendFactory;
import net.dv8tion.jda.core.entities.Game;
import okhttp3.OkHttpClient;
import org.slf4j.ILoggerFactory;

import javax.annotation.Nonnull;
import java.util.function.IntFunction;

@SuppressWarnings("WeakerAccess")
public class ModularBuilder {

    protected final String token;
    protected int shards = -1;
    protected IConfigHandler configHandler = null;
    protected ILoggerFactory loggerFactory = null;
    protected ICommandHandler commandHandler = null;
    protected IAudioSendFactory audioSendFactory = null;
    protected BiIntFunction<Game> readyStatusProvider = null;
    protected IntFunction<OnlineStatus> onlineStatusProvider = null;
    protected OkHttpClient.Builder httpClientBuilder = null;
    protected int corePoolSize = 2;
    protected boolean useDecoratorCache = false;
    protected boolean useAudio = true;
    protected boolean useStats = false;
    protected boolean useWebhooks = false;
    protected boolean useBulkDeleteSplitting = true;
    protected boolean useShutdownNow = false;

    /**
     * Create a completely new Builder with a token.
     */
    public ModularBuilder(@Nonnull String token) {
        this.token = token;
    }

    @Nonnull
    public ModularBuilder useManualSharding(int shardCount) {
        shards = shardCount;
        return this;
    }

    @Nonnull
    public ModularBuilder setConfigHandler(@Nonnull IConfigHandler handler) {
        configHandler = handler;
        return this;
    }

    @Nonnull
    public ModularBuilder setLoggerFactory(@Nonnull ILoggerFactory handler) {
        loggerFactory = handler;
        return this;
    }

    @Nonnull
    public ModularBuilder setCommandHandler(@Nonnull ICommandHandler handler) {
        commandHandler = handler;
        return this;
    }

    @Nonnull
    public ModularBuilder setAudioSendFactory(@Nonnull IAudioSendFactory factory) {
        audioSendFactory = factory;
        return this;
    }

    @Nonnull
    public ModularBuilder setReadyStatusProvider(@Nonnull BiIntFunction<Game> provider) {
        readyStatusProvider = provider;
        return this;
    }

    @Nonnull
    public ModularBuilder setOnlineStatusProvider(@Nonnull IntFunction<OnlineStatus> provider) {
        onlineStatusProvider = provider;
        return this;
    }

    @Nonnull
    public ModularBuilder setHttpClientBuilder(@Nonnull OkHttpClient.Builder builder) {
        httpClientBuilder = builder;
        return this;
    }

    @Nonnull
    public ModularBuilder setCorePoolSize(int size) {
        corePoolSize = size;
        return this;
    }

    @Nonnull
    public ModularBuilder allowDecoratorCacheForDismissible(boolean on) {
        useDecoratorCache = on;
        return this;
    }

    @Nonnull
    public ModularBuilder setEnableAudio(boolean on) {
        useAudio = on;
        return this;
    }

    @Nonnull
    public ModularBuilder setEnableStats(boolean on) {
        useStats = on;
        return this;
    }

    @Nonnull
    public ModularBuilder setEnableWebhooks(boolean on) {
        useWebhooks = on;
        return this;
    }

    @Nonnull
    public ModularBuilder setEnableMessageBulkDeleteSplitting(boolean on) {
        useBulkDeleteSplitting = on;
        return this;
    }

    @Nonnull
    public ModularBuilder setEnableShutdownNow(boolean on) {
        useShutdownNow = on;
        return this;
    }

    public ModularBot build() {
        try {
            return new ModularBot(token, shards, configHandler, commandHandler, loggerFactory,
                    readyStatusProvider, onlineStatusProvider, useDecoratorCache, useAudio, useStats, useWebhooks,
                    useBulkDeleteSplitting, useShutdownNow, audioSendFactory, httpClientBuilder, corePoolSize);
        } catch (Exception e) {
            System.err.println("An error occurred while initializing ModularBot !");
            e.printStackTrace();
            return null;
        }
    }
}
