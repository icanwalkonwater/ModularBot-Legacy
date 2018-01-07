package com.jesus_crie.modularbot.utils;

import com.jesus_crie.modularbot.ModularBot;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.Webhook;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.utils.Checks;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;

@Deprecated
public class Webhooks {

    /**
     * Overload of {@link #execute(Webhook, String, String, boolean, String, MessageEmbed[])}.
     */
    public static RestAction<Void> execute(Webhook hook, String content) {
        return execute(hook, null, null, false, content, null);
    }

    /**
     * Overload of {@link #execute(Webhook, String, String, boolean, String, MessageEmbed[])}.
     */
    public static RestAction<Void> execute(Webhook hook, String user, String avatar, String content) {
        return execute(hook, user, avatar, false, content, null);
    }

    /**
     * Overload of {@link #execute(Webhook, String, String, MessageEmbed...)}.
     */
    public static RestAction<Void> execute(Webhook hook, MessageEmbed... embeds) {
        return execute(hook, null, null, embeds);
    }

    /**
     * Overload of {@link #execute(Webhook, String, String, boolean, String, MessageEmbed[])}.
     */
    public static RestAction<Void> execute(Webhook hook, String user, String avatar, MessageEmbed... embeds) {
        return execute(hook, user, avatar, false, null, embeds);
    }

    /**
     * Can be used to execute any webhook (which is not by default in JDA, don't ask).
     * If the bot isn't ready, an {@link net.dv8tion.jda.core.requests.RestAction.EmptyRestAction} will be returned.
     * @param hook the webhook to execute.
     * @param user (optional) the username that will be displayed instead of the webhook's name.
     * @param avatar (optional) the url of the avatar that will be displayed instead if the webhook's image.
     * @param isTTS is the message a text to speech message ?
     * @param content (optional if embeds aren't null) the content of the message.
     * @param embeds (optional if content isn't empty) the embeds to send with the content, yes you can send more than 1 embed with a webhook !
     * @return a {@link RestAction} that will execute the webhook with the given parameters.
     */
    @SuppressWarnings({"UnusedAssignment", "ResultOfMethodCallIgnored"})
    public static RestAction<Void> execute(Webhook hook, String user, String avatar, boolean isTTS, String content, MessageEmbed[] embeds) {

        if (!ModularBot.isReady())
            return new RestAction.EmptyRestAction<>(hook.getJDA(), null);

        Checks.notNull(hook, "webhook");
        if ((content == null || content.isEmpty()) && embeds == null)
            throw new IllegalArgumentException("One of those is required: content, embeds");

        JSONObject body = new JSONObject();
        if (content != null)
            body.put("content", content);
        if (user != null)
            body.put("username", user);
        if (avatar != null)
            body.put("avatar_url", avatar);
        if (isTTS)
            body.put("tts", true);
        if (embeds != null) {
            JSONArray jsonEmbeds = new JSONArray();
            Arrays.stream(embeds)
                    .map(MessageEmbed::toJSONObject)
                    .forEach(jsonEmbeds::put);
            body.put("embeds", jsonEmbeds);
        }

        final Route.CompiledRoute route = Route.Webhooks.EXECUTE_WEBHOOK.compile(hook.getId(), hook.getToken());
        return new RestAction<Void>(hook.getJDA(), route, body) {
            @Override
            protected void handleResponse(Response response, Request<Void> request) {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }
}
