import com.jesus_crie.modularbot.config.IConfigHandler;
import com.jesus_crie.modularbot.listener.ICommandHandler;
import com.jesus_crie.modularbot.manager.CommandManager;
import com.jesus_crie.modularbot.manager.MessageDecoratorManager;
import com.jesus_crie.modularbot.manager.ModularEventManager;
import com.jesus_crie.modularbot.utils.BiIntFunction;
import com.jesus_crie.modularbot.utils.ModularThreadFactory;
import net.dv8tion.jda.bot.sharding.DefaultShardManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.audio.factory.IAudioSendFactory;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.webhook.WebhookCluster;
import okhttp3.OkHttpClient;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import java.util.function.IntFunction;

@SuppressWarnings("WeakerAccess")
public class ModularBot extends DefaultShardManager {

    private static ModularBot INSTANCE = null;
    private static Logger logger;

    @Nonnull
    public static ModularBot getSingleton() {
        if (INSTANCE == null)
            throw new IllegalStateException("You need to create a instance first using ModularBuilder !");
        return INSTANCE;
    }

    protected final String token;
    protected final BiIntFunction<Game> gameProvider;
    protected final IConfigHandler configHandler;
    protected final CommandManager commandManager;
    protected final MessageDecoratorManager decoratorManager;
    protected final WebhookCluster webhookCluster;

    /**
     * Build a new instance of {@link ModularBot}, this can only be called through {@link ModularBuilder#build()}.
     * Only one instance is allowed ! You will get an error if you try to instantiate this several times.
     *
     * @param token                  The token of your application.
     * @param shardTotal             The amount of shard to use or {@code -1} to retrieve it automatically.
     * @param configHandler          The {@link IConfigHandler} to use.
     * @param commandHandler         The {@link ICommandHandler} to use.
     * @param loggerFactory          The {@link ILoggerFactory} to use instead of the default one, or {@code null}.
     * @param gameProvider           The provider for the game to display when the bot is ready to use.
     * @param statusProvider         The provider for the {@link OnlineStatus} to display.
     * @param useDecoratorCache      Whether the decorator cache should be used for {@link com.jesus_crie.modularbot.messagedecorator.dismissible.DismissibleDecorator}.
     * @param useAudio               Whether the audio will be enable or not.
     * @param useStats               Whether the stat system should run or not.
     * @param useWebhooks            Whether the webhooks should be supported or not.
     * @param useBulkDeleteSplitting Whether the {@link net.dv8tion.jda.core.events.message.MessageBulkDeleteEvent} should be splitted in multiple {@link net.dv8tion.jda.core.events.message.MessageDeleteEvent}.
     * @param useShutdownNow         Whether {@link JDA#shutdownNow()} should be used or not.
     * @param audioSendFactory       The {@link IAudioSendFactory} to use.
     * @param httpBuilder            The {@link OkHttpClient.Builder} to use.
     * @param corePoolSize           The core pool size of JDA's internal executor.
     */
    ModularBot(@Nonnull final String token, final int shardTotal,
               @Nonnull final IConfigHandler configHandler, @Nonnull final ICommandHandler commandHandler, @Nullable final ILoggerFactory loggerFactory,
               @Nullable final BiIntFunction<Game> gameProvider, @Nullable final IntFunction<OnlineStatus> statusProvider,
               final boolean useDecoratorCache, final boolean useAudio, final boolean useStats, final boolean useWebhooks,
               final boolean useBulkDeleteSplitting, final boolean useShutdownNow,
               @Nonnull final IAudioSendFactory audioSendFactory, @Nonnull final OkHttpClient.Builder httpBuilder, @Nonnegative final int corePoolSize) throws Exception {

        super(shardTotal, null, null, null,
                token, new ModularEventManager(),
                audioSendFactory,
                null, statusProvider,
                httpBuilder, null,
                new ModularThreadFactory("Global"),
                null, 900, corePoolSize,
                useAudio, true, useBulkDeleteSplitting, true,
                null,
                true, useShutdownNow,
                false, null);

        INSTANCE = this;

        if (loggerFactory != null) StaticLoggerBinder.getSingleton().setLoggerFactory(loggerFactory);
        logger = LoggerFactory.getLogger("Global");
        logger.info("Logger successfully setup !");

        logger.info("Initializing components...");

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> logger.error("UNCAUGHT EXCEPTION", e));
        this.token = token;
        commandManager = new CommandManager(commandHandler);
        this.gameProvider = gameProvider;

        logger.info("Loading config...");
        this.configHandler = configHandler;
        configHandler.load();

        if (useWebhooks) {
            logger.info("Setting up webhooks...");
            webhookCluster = new WebhookCluster();
            webhookCluster.setDefaultThreadFactory(new ModularThreadFactory("Webhook"));
        } else webhookCluster = null;

        logger.info("Preparing decorator manager...");
        decoratorManager = new MessageDecoratorManager(useDecoratorCache);

        logger.info("Initialization completed !");
    }

    @Override
    public void login() throws LoginException {
        logger.info("Starting " + shardsTotal + " shards...");
        super.login();
    }

    public void dispatchRequest(@Nonnull Consumer<JDA> action) {
        shards.forEach(action);
    }

    @Override
    protected ScheduledExecutorService createExecutor(ThreadFactory threadFactory) {
        return Executors.newScheduledThreadPool(2, r -> {
            Thread t = threadFactory.newThread(r);
            t.setPriority(Thread.NORM_PRIORITY + 1);
            return t;
        });
    }
}
