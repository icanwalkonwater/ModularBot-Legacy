package com.jesus_crie.modularbot.sharding;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

import javax.security.auth.login.LoginException;

public class ModularShard {

    private JDA jda;

    public ModularShard(JDA shard) throws LoginException, InterruptedException, RateLimitedException {
        jda = shard;
    }

    public void restartJDA() {

    }

    public JDA getJDA() {
        return jda;
    }
}
