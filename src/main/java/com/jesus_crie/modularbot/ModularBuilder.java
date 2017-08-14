package com.jesus_crie.modularbot;

import com.jesus_crie.modularbot.config.ConfigHandler;
import com.jesus_crie.modularbot.config.SimpleConfig;
import com.jesus_crie.modularbot.config.Version;
import com.jesus_crie.modularbot.listener.CommandEvent;
import com.jesus_crie.modularbot.listener.CommandHandler;
import com.jesus_crie.modularbot.listener.DefaultCommandHandler;
import com.jesus_crie.modularbot.log.ConsoleLogger;
import com.jesus_crie.modularbot.log.DefaultLogger;
import com.jesus_crie.modularbot.log.Log;
import com.jesus_crie.modularbot.log.Logger;
import com.jesus_crie.modularbot.stats.Stats;

public class ModularBuilder {

    private final String token;
    private ConfigHandler config;
    private Logger logger;
    private CommandHandler command;
    private boolean useAudio = false;
    private boolean useStats = false;

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
     * @param handler your custom implementation of {@link Logger}.
     * @return the current builder.
     */
    public ModularBuilder useCustomLogHandler(Logger handler) {
        logger = handler;
        return this;
    }

    /**
     * If you want to customize the way that command errors are handled and printed.
     * It can also act like a middleware with {@link CommandHandler#onCommand(CommandEvent)}.
     * @param handler an implementation of {@link CommandHandler}.
     * @return the current builder.
     */
    public ModularBuilder useCustomCommandHandler(CommandHandler handler) {
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
     * Create an instance of {@link ModularBot} with the parameter given before
     * or default values and handlers such as {@link SimpleConfig} and {@link DefaultLogger}.
     * @return a new instance of {@link ModularBot}.
     */
    public ModularBot build() {
        if (ModularBot.instance() != null)
            throw new IllegalStateException("Modular Bot can only be instantiated one time !");

        if (config == null)
            config = new SimpleConfig("./config.json", new Version(1, 0, 0, 0), "ModularBot");
        if (logger == null)
            logger = new DefaultLogger();
        logger.registerListener(new ConsoleLogger());
        if (command == null)
            command = new DefaultCommandHandler();
        if (useStats)
            Stats.enable();

        return new ModularBot(token, config, logger, command, useAudio);
    }
}
