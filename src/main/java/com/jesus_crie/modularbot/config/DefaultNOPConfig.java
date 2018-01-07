package com.jesus_crie.modularbot.config;

import net.dv8tion.jda.core.entities.Guild;

import java.io.IOException;

public class DefaultNOPConfig implements IConfigHandler {

    @Override
    public Version getVersion() {
        return Version.of(1, 0, 0, 0);
    }

    @Override
    public String getPrefixForGuild(Guild g) {
        return "/";
    }

    @Override
    public String getAppName() {
        return "ModularBot";
    }

    @Override
    public long getCreatorId() {
        return 0;
    }

    @Override
    public void load() throws IOException {
        // NOP
    }

    @Override
    public void save() throws IOException {
        // NOP
    }

    @Override
    public void startAutoSave() {
        // NOP
    }
}
