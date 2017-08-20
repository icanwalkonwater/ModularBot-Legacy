package com.jesus_crie.modularbot.log;

import com.jesus_crie.modularbot.template.EmbedTemplate;
import com.jesus_crie.modularbot.utils.Icons;
import com.jesus_crie.modularbot.utils.Webhooks;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Webhook;

import java.awt.*;
import java.time.Instant;

import static com.jesus_crie.modularbot.utils.F.f;

public class WebhookLogger implements LogListener {

    private static final EmbedTemplate template = new EmbedTemplate(new EmbedBuilder()
            .setAuthor("{0}", null, Icons.INFORMATION)
            .setDescription("{1}")
            .setFooter("{2}", null));
    private final Webhook hook;

    public WebhookLogger(Webhook webhook) {
        hook = webhook;
    }

    @Override
    public void onLog(Log log) {
        String message = message = log.MESSAGE;
        if (message.length() > 1500)
            message = message.substring(0, 1495) + "\n...";

        EmbedBuilder builder = template.format(log.PREFIX, log.CONTENT == null ? message : f("%s: %s", message, log.CONTENT), log.THREAD_NAME)
                .setColor(Color.GREEN)
                .setTimestamp(Instant.now());
        Webhooks.execute(hook, log.LEVEL.toString(), Icons.CHECK, builder.build()).complete();
    }

    @Override
    public void onError(Log log) {
        String message = log.MESSAGE;
        if (message.length() > 1500)
            message = message.substring(0, 1495) + "\n...";

        EmbedBuilder builder = template.format(log.PREFIX, log.CONTENT == null ? message : f("%s: %s", message, log.CONTENT), log.THREAD_NAME)
                .setColor(Color.RED)
                .setTimestamp(Instant.now());
        Webhooks.execute(hook, log.LEVEL.toString(), Icons.ERROR, builder.build()).complete();
    }
}
