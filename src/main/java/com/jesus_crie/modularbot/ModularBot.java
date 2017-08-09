package com.jesus_crie.modularbot;

import com.jesus_crie.modularbot.config.ConfigHandler;
import com.jesus_crie.modularbot.manager.ModularEventManager;
import com.jesus_crie.modularbot.manager.ThreadManager;
import com.jesus_crie.modularbot.sharding.ModularShard;
import com.jesus_crie.modularbot.utils.Status;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.jesus_crie.modularbot.utils.F.f;

public class ModularBot {

    private static ModularBot instance;

    private final String token;
    private final String appName;
    private final ConfigHandler config;
    private final List<ModularShard> shards;

    ModularBot(String token, String appName, ConfigHandler config, boolean useSharding, boolean useAudio) throws LoginException, RateLimitedException, InterruptedException {
        instance = this;
        ThreadManager.init(appName);
        this.token = token;
        this.appName = appName;
        this.config = config;

        if (useSharding) {
            shards = new ArrayList<>();
            int totalShard = getShardMax();

            for (int i = 1; i < totalShard; i++) {
                shards.add(new ModularShard(new JDABuilder(AccountType.BOT)
                        .setToken(token)
                        .useSharding(i, totalShard)
                        .setAutoReconnect(true)
                        .setAudioEnabled(useAudio)
                        .setEventManager(new ModularEventManager())
                        .setIdle(false)
                        .setGame(Status.STARTING)
                        .buildBlocking()));
            }
        } else {
            shards = Collections.singletonList(new ModularShard(new JDABuilder(AccountType.BOT)
                    .setToken(token)
                    .setAutoReconnect(true)
                    .setAudioEnabled(useAudio)
                    .setEventManager(new ModularEventManager())
                    .setIdle(false)
                    .setGame(Status.STARTING)
                    .buildBlocking()));
        }
    }

    public <T> List<T> collectShardInfos(Function<ModularShard, T> action) {
        return shards.stream()
                .map(action)
                .collect(Collectors.toList());
    }

    public <T> T collectCumulativeShardInfos(Function<ModularShard, T> action, Collector<T, T, T> mapper) {
        return shards.stream()
                .map(action)
                .collect(mapper);
    }

    public List<ModularShard> getShards() {
        return shards;
    }

    private int getShardMax() {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("https://discordapp.com/gateway/bot")
                    .addHeader("Authorization", f("Bot %s", token))
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response res = client.newCall(request).execute();
            JSONObject j = new JSONObject(res.body().string());
            res.close();

            return j.getInt("shards");
        } catch (IOException ignore) {}

        return 1;
    }

    public static ModularBot instance() {
        return instance;
    }
}
