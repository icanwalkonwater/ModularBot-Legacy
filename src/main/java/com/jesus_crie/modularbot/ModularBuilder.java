package com.jesus_crie.modularbot;

import com.jesus_crie.modularbot.config.ConfigHandler;
import com.jesus_crie.modularbot.config.SimpleConfig;
import com.jesus_crie.modularbot.config.Version;
import com.jesus_crie.modularbot.log.LogHandler;
import com.jesus_crie.modularbot.stats.Stats;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

import javax.security.auth.login.LoginException;
import java.io.IOException;

public class ModularBuilder {

    private String token;
    private String appName;
    private ConfigHandler config;
    private LogHandler logger;
    private boolean useSharding = false;
    private boolean useAudio = false;
    private boolean useStats = false;

    public ModularBuilder(String token) {
        this.token = token;
    }

    public ModularBuilder setName(String appName) {
        this.appName = appName;
        return this;
    }

    public ModularBuilder useCustomConfigHandler(ConfigHandler handler) {
        config = handler;
        return this;
    }

    public ModularBuilder useCustomLogHandler(LogHandler handler) {
        logger = handler;
        return this;
    }

    public ModularBuilder useSharding() {
        useSharding = true;
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

    public ModularBot build() throws LoginException, InterruptedException, RateLimitedException {
        if (config == null) {
            try {
                config = new SimpleConfig("./config.json", new Version(1, 0, 0, 0));
            } catch (IOException ignore) {}
        }

        if (useStats)
            Stats.enable();

        return new ModularBot(token,
                appName == null ? "ModularBot" : appName,
                config,
                useSharding,
                useAudio);
    }
}
