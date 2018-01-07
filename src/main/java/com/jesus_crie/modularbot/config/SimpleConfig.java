package com.jesus_crie.modularbot.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jesus_crie.modularbot.ModularBot;
import net.dv8tion.jda.core.entities.Guild;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * A simple config wrapper that load config from a file called "config.json" using an {@link ObjectMapper}.
 * It can store any object but for complex object it's better to use the Jackson Databind serialization system with {@link com.fasterxml.jackson.databind.ser.std.StdSerializer}.
 */
public class SimpleConfig implements IConfigHandler {

    protected static final ObjectMapper mapper = new ObjectMapper();

    protected final File configFile;
    private final Version version;
    private final String appName;
    protected final HashMap<String, JsonNode> settings = new HashMap<>();

    public SimpleConfig(String configFile, Version version, String appName) {
        this.version = version;
        this.appName = appName;
        this.configFile = new File(configFile);
    }

    /**
     * @see IConfigHandler#getVersion()
     */
    @Override
    public Version getVersion() {
        return version;
    }

    /**
     * @see IConfigHandler#getPrefixForGuild(Guild)
     */
    @Override
    public String getPrefixForGuild(Guild g) {
        return "/";
    }

    /**
     * @see IConfigHandler#getAppName()
     */
    @Override
    public String getAppName() {
        return appName;
    }

    /**
     * My personal Id.
     * I recommend to override this.
     * @see IConfigHandler#getCreatorId()
     */
    @Override
    public long getCreatorId() {
        return 182547138729869314L;
    }

    /**
     * Used to get any string from the config.
     * @param name The key name.
     * @return the value as a string or null if not found.
     */
    public String getSettingAsString(String name) {
        return settings.get(name).asText(null);
    }

    /**
     * Used to get a setting as a long (an id for example).
     * @param name The key name.
     * @return the value as a long or 0 if not found.
     */
    public Long getSettingAsLong(String name) {
        return settings.get(name).asLong();
    }

    /**
     * Used to get a setting as a complex object (a list for example)
     * @param name The key name.
     * @return the value as a {@link JsonNode} or null if not found.
     */
    public JsonNode getSettingAsJsonObject(String name) {
        return settings.get(name);
    }

    /**
     * Create the config file only if it doesn't exist yet.
     * @throws IOException if something is wrong.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void checkAndCreateFile() throws IOException {
        if (!configFile.exists()) {
            ModularBot.logger().warning("Config", "No config file, creating one.");
            FileUtils.write(configFile, "{}", Charset.forName("UTF-8"));
            return;
        }

        BufferedReader reader = new BufferedReader(new FileReader(configFile));
        if (reader.readLine() == null) { // If file empty
            ModularBot.logger().warning("Config", "Empty config file founded, fixing this...");
            FileUtils.write(configFile, "{}", Charset.forName("UTF-8"));
        }
    }

    /**
     * @see IConfigHandler#load()
     */
    @Override
    public void load() throws IOException {
        checkAndCreateFile();

        JsonNode node = mapper.readValue(configFile, JsonNode.class);
        node.fields().forEachRemaining(entry -> settings.put(entry.getKey(), entry.getValue()));
    }

    /**
     * @see IConfigHandler#save()
     */
    @Override
    public void save() throws IOException {
        mapper.writeValue(configFile, settings);
    }

    /**
     * @see IConfigHandler#startAutoSave()
     */
    @Override
    public void startAutoSave() {
        ModularBot.instance().getMightyPool().scheduleWithFixedDelay(() -> {
            try {
                save();
            } catch (IOException ignore) {}
        }, 5, 5, TimeUnit.MINUTES);
    }
}
