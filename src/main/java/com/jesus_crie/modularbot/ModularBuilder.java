package com.jesus_crie.modularbot;

import com.jesus_crie.modularbot.config.ConfigHandler;
import com.jesus_crie.modularbot.config.SimpleConfig;
import com.jesus_crie.modularbot.config.Version;
import com.jesus_crie.modularbot.listener.CommandEvent;
import com.jesus_crie.modularbot.listener.DefaultModularCommandListener;
import com.jesus_crie.modularbot.listener.ModularCommandListener;
import com.jesus_crie.modularbot.log.ConsoleLogger;
import com.jesus_crie.modularbot.log.DefaultLogHandler;
import com.jesus_crie.modularbot.log.Log;
import com.jesus_crie.modularbot.log.LogHandler;
import com.jesus_crie.modularbot.stats.Stats;
import net.dv8tion.jda.core.entities.Game;

import static com.jesus_crie.modularbot.utils.F.f;

public class ModularBuilder {

    private final String token;
    private ConfigHandler config;
    private LogHandler logger;
    private ModularCommandListener command;
    private boolean useAudio = false;
    private boolean useStats = false;
    private boolean useDecoratorCache = false;
    private boolean useWebhook = false;
    private Game readyStatus;

    /**
     * Some peoples may prefer this way to create the builder.
     * Its the same as doing
     * <code>new ModularBuilder(token)</code>
     * @param token the token that will be used to log into discord.
     * @return a new {@link ModularBuilder}
     */
    public static ModularBuilder create(String token) {
        return new ModularBuilder(token);
    }

    /**
     * Default constructor.
     * @param token the token that will be used to log into Discord.
     */
    public ModularBuilder(String token) {
        this.token = token;
    }

    /**
     * Use if you want to use your custom config system.
     * @param handler any object that implements {@link ConfigHandler}
     * @return the current builder.
     */
    public ModularBuilder useCustomConfigHandler(ConfigHandler handler) {
        config = handler;
        return this;
    }

    /**
     * Use if you want to customize the way that the logs are handled.
     * To modify the way that logs are printed you need to override {@link Log#toString()}
     * and use a custom handle to instantiate your overrided version of {@link Log}.
     * @param handler your custom implementation of {@link LogHandler}.
     * @return the current builder.
     */
    public ModularBuilder useCustomLogHandler(LogHandler handler) {
        logger = handler;
        return this;
    }

    /**
     * If you want to customize the way that command errors are handled and printed.
     * It can also act like a middleware with {@link ModularCommandListener#onCommand(CommandEvent)}.
     * @param handler an implementation of {@link ModularCommandListener}.
     * @return the current builder.
     */
    public ModularBuilder useCustomCommandHandler(ModularCommandListener handler) {
        command = handler;
        return this;
    }

    /**
     * Enable the audio.
     * @return the current builder.
     */
    public ModularBuilder useAudio() {
        useAudio = true;
        return this;
    }

    /**
     * Allow Modular and you to use {@link Stats} locally.
     * It's for <b>your</b> stats, not mines.
     * Nothing will be uploaded or something.
     * @return the current builder.
     */
    public ModularBuilder useStats() {
        useStats = true;
        return this;
    }

    /**
     * If you want to override the default ready message displayed by your bot.
     * @param status the {@link Game} to show.
     * @return the current builder.
     */
    public ModularBuilder useCustomReadyStatus(Game status) {
        readyStatus = status;
        return this;
    }

    /**
     * TODO - Work in progress
     *
     * Allow Modular to save decorated message to be able to restore them after a restart.
     * Useful for panel messages or polls.
     * @return the current builder.
     */
    private ModularBuilder useDecoratorCache() {
        useDecoratorCache = true;
        return this;
    }

    /**
     * Use to allow the usage of webhook in the application.
     * @return the current builder.
     */
    public ModularBuilder useWebhooks() {
        useWebhook = true;
        return this;
    }

    /**
     * Create an instance of {@link ModularBot} with the parameter given before
     * or default values and handlers such as {@link SimpleConfig} and {@link DefaultLogHandler}.
     * @return a new instance of {@link ModularBot}.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public ModularBot build() {
        try {
            ModularBot.instance();
            throw new IllegalStateException("Modular Bot can only be instantiated one time !");
        } catch (Exception ignore) {}

        if (config == null)
            config = new SimpleConfig("./config.json", new Version(1, 0, 0, 0), "ModularBot");
        if (logger == null)
            logger = new DefaultLogHandler();
        logger.registerListener(new ConsoleLogger());
        if (command == null)
            command = new DefaultModularCommandListener();
        if (useStats)
            Stats.setEnable(true);
        if (readyStatus == null)
            readyStatus = Game.of(f("%shelp - v%s", config.getPrefixForGuild(null), config.getVersion().toString()));

        return new ModularBot(token, config, logger, command, readyStatus, useAudio, useDecoratorCache, useWebhook);
    }
}
