package com.jesus_crie.modularbot.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.messagedecorator.Cacheable;
import com.jesus_crie.modularbot.messagedecorator.ReactionDecorator;
import com.jesus_crie.modularbot.messagedecorator.dismissible.DismissibleDecorator;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class DecoratorCache {

    private final boolean allowDismissible;
    private final CopyOnWriteArrayList<ReactionDecorator> decorators = new CopyOnWriteArrayList<>(); // Concurrent ArrayList

    private final File file;
    private final ObjectMapper mapper;

    public DecoratorCache(boolean saveDismissible) {
        allowDismissible = saveDismissible;
        file = new File("./modular_cache.json");
        mapper = new ObjectMapper();

        try {
            checkAndCreateFile();
        } catch (IOException e) {
            ModularBot.logger().fatal("Decorator Cache", "Failed to initialize cache file !");
            ModularBot.logger().error("Decorator Cache", e);
        }
    }

    /**
     * Get the amount of decorators currently stored in the cache.
     * @return the size of the cache.
     */
    public int size() {
        return decorators.size();
    }

    private void checkAndCreateFile() throws IOException {
        if (!file.exists()) {
            ModularBot.logger().warning("Decorator Cache", "No cache file ! Creating one...");
            FileUtils.write(file, "[]", Charset.forName("UTF-8"));
            return;
        }

        BufferedReader reader = new BufferedReader(new FileReader(file));
        if (reader.readLine() == null) { // If file empty
            FileUtils.write(file, "[]", Charset.forName("UTF-8"));
        }
    }

    /**
     * Try to cache a decorator. If the decorator is dismissible and the cache is disabled it will do nothing.
     * @param decorator the decorator to cache.
     */
    public void tryCacheDecorator(ReactionDecorator decorator) {
        if (!(decorator instanceof Cacheable)) return;
        if (decorator instanceof DismissibleDecorator && !allowDismissible) return;
        decorators.add(decorator);
    }

    /**
     * Remove a decorator from the cache.
     * @param decorator the decorator to remove.
     */
    public void uncacheDecorator(ReactionDecorator decorator) {
        decorators.remove(decorator);
    }

    /**
     * Save the cache to the config file.
     * Save only the decorators who will stand more than 10 seconds.
     */
    public void saveCache() {
        try {
            mapper.writeValue(file, decorators.stream()
                    .filter(d -> !(d instanceof DismissibleDecorator && ((DismissibleDecorator) d).getExpireTime() < System.currentTimeMillis() + 10000))
                    .collect(Collectors.toList()));
        } catch (IOException e) {
            ModularBot.logger().fatal("Decorator Cache", "Failed to save cache !");
            ModularBot.logger().error("Decorator Cache", e);
        }
    }

    /**
     * Load and resume the cached decorators.
     */
    public void loadAndResumeCache() {
        try {
            JsonNode root = mapper.readValue(file, JsonNode.class);
            if (!root.isArray()) throw new IOException("The root object is not an array !");

            for (JsonNode node : root) { // For each saved decorator.
                long msgId = node.get("message").asLong();
                String source = node.get("source").asText();

                Message message = deserializeSource(source, msgId);
                if (message == null) continue; // If the channel or the message no longer exist or unknown source.

                try {
                    Class<?> clazz = Class.forName(node.get("@class").asText());
                    Constructor c = clazz.getDeclaredConstructor(Message.class, JsonNode.class);
                    c.setAccessible(true);
                    c.newInstance(message, node);
                } catch (InvocationTargetException e) {
                    ModularBot.logger().warning("Decorator Cache", e.getTargetException().toString());
                } catch (ReflectiveOperationException e) {
                    ModularBot.logger().warning("Decorator Cache", e.toString());
                }
            }

            ModularBot.logger().info("Decorator Cache", "All decorators successfully loaded ! (" + decorators.size() + ")");
        } catch (IOException e) {
            ModularBot.logger().fatal("Decorator Cache", "Failed to load cache !");
            ModularBot.logger().error("Decorator Cache", e);
        }
    }

    public static String serializeSource(Message message) {
        switch (message.getChannelType()) {
            case TEXT:
                return "G" + message.getGuild().getIdLong();
            case PRIVATE:
                return "P" + message.getPrivateChannel().getIdLong();
            default: // No group message because bots can't be in group message
                return "?" + message.getChannel().getIdLong();
        }
    }

    public static Message deserializeSource(String source, long msgId) {
        final long id = Long.parseLong(source.substring(1));
        final ModularBot bot = ModularBot.instance();

        final MessageChannel channel;
        switch (source.charAt(0)) {
            case 'G':
                channel = bot.getTextChannelById(id);
                break;
            case 'P':
                channel = bot.getPrivateChannelById(id);
                break;
            default:
                channel = null;
                break;
        }

        if (channel == null) return null;

        return channel.getMessageById(msgId).complete();
    }
}
