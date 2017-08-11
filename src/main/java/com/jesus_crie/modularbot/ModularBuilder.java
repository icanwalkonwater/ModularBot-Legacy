package com.jesus_crie.modularbot;

import com.jesus_crie.modularbot.config.ConfigHandler;
import com.jesus_crie.modularbot.config.SimpleConfig;
import com.jesus_crie.modularbot.config.Version;
import com.jesus_crie.modularbot.log.ConsoleLogger;
import com.jesus_crie.modularbot.log.DefaultLogger;
import com.jesus_crie.modularbot.log.Logger;
import com.jesus_crie.modularbot.stats.Stats;

public class ModularBuilder {

    private String token;
    private String appName;
    private ConfigHandler config;
    private Logger logger;
    private boolean useAudio = false;
    private boolean useStats = false;

    public ModularBuilder(String token) {
        this.token = token;
    }

    public ModularBuilder useCustomConfigHandler(ConfigHandler handler) {
        config = handler;
        return this;
    }

    public ModularBuilder useCustomLogHandler(Logger handler) {
        logger = handler;
        return this;
    }

    public ModularBuilder useAudio() {
        useAudio = true;
        return this;
    }

    public ModularBuilder useStats() {
        useStats = true;
        return this;
    }

    public ModularBot build() {
        if (config == null)
            config = new SimpleConfig("./config.json", new Version(1, 0, 0, 0), "ModularBot");
        if (logger == null)
            logger = new DefaultLogger();
        logger.registerListener(new ConsoleLogger());
        if (useStats)
            Stats.enable();

        return new ModularBot(token,
                config,
                logger,
                useAudio);
    }
}
