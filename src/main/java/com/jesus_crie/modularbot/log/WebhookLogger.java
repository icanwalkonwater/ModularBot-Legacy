package com.jesus_crie.modularbot.log;

import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.template.EmbedTemplate;
import com.jesus_crie.modularbot.utils.Icons;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Webhook;
import net.dv8tion.jda.webhook.WebhookClient;
import net.dv8tion.jda.webhook.WebhookMessageBuilder;

import java.awt.Color;
import java.time.Instant;

import static com.jesus_crie.modularbot.utils.F.f;

public class WebhookLogger implements LogListener {

    private static final EmbedTemplate template = new EmbedTemplate(new EmbedBuilder()
            .setAuthor("{0}", null, Icons.INFORMATION)
            .setDescription("{1}")
            .setFooter("{2}", null)
            .build());
    private final WebhookClient hook;

    public WebhookLogger(Webhook webhook) {
        hook = ModularBot.instance().createWebHookClient(webhook);
    }

    @Override
    public void onLog(Log log) {
        String message = log.MESSAGE;
        if (message.length() > 1500)
            message = message.substring(0, 1495) + "\n...";

        EmbedBuilder builder = template.format(log.PREFIX, log.CONTENT == null ? message : f("%s: %s", message, log.CONTENT), log.THREAD_NAME)
                .setColor(Color.GREEN)
                .setTimestamp(Instant.now());
        try {
            hook.send(new WebhookMessageBuilder()
                    .setUsername(log.LEVEL.toString())
                    .setAvatarUrl(Icons.CHECK)
                    .addEmbeds(builder.build())
                    .build()).get();
        } catch (Exception ignore) {}
    }

    @Override
    public void onError(Log log) {
        String message = log.MESSAGE;
        if (message.length() > 1500)
            message = message.substring(0, 1495) + "\n...";

        EmbedBuilder builder = template.format(log.PREFIX, log.CONTENT == null ? message : f("%s: %s", message, log.CONTENT), log.THREAD_NAME)
                .setColor(Color.RED)
                .setTimestamp(Instant.now());

        try {
            hook.send(new WebhookMessageBuilder()
                    .setUsername(log.LEVEL.toString())
                    .setAvatarUrl(Icons.ERROR)
                    .addEmbeds(builder.build())
                    .build()).get();
        } catch (Exception ignore) {}
    }
}
