package com.jesus_crie.modularbot.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jesus_crie.modularbot.ModularBot;
import net.dv8tion.jda.core.entities.Guild;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * A simple config wrapper that load config from a file called "config.json" using an {@link ObjectMapper}.
 * It can store any object but for complex object it's better to use the Jackson Databind serialization system with {@link com.fasterxml.jackson.databind.ser.std.StdSerializer}.
 */
public class SimpleConfig implements ConfigHandler {

    private static final ObjectMapper mapper = new ObjectMapper();

    private final File configFile;
    private final Version version;
    private final String appName;
    private final HashMap<String, JsonNode> settings = new HashMap<>();

    public SimpleConfig(String configFile, Version version, String appName) {
        this.version = version;
        this.appName = appName;
        this.configFile = new File(configFile);
    }

    /**
     * @see ConfigHandler#getVersion()
     */
    @Override
    public Version getVersion() {
        return version;
    }

    /**
     * @see ConfigHandler#getPrefixForGuild(Guild)
     */
    @Override
    public String getPrefixForGuild(Guild g) {
        return "/";
    }

    /**
     * @see ConfigHandler#getAppName()
     */
    @Override
    public String getAppName() {
        return appName;
    }

    /**
     * My personal Id.
     * I recommend to override this.
     * @see ConfigHandler#getCreatorId()
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
     * @return true if the file has been successfully created.
     *      False if the file already exist.
     */
    @SuppressWarnings("UnusedReturnValue")
    private boolean checkAndCreateFile() throws IOException {
        return configFile.createNewFile();
    }

    /**
     * @see ConfigHandler#load()
     */
    @Override
    public void load() throws IOException {
        checkAndCreateFile();

        JsonNode node = mapper.readValue(configFile, JsonNode.class);
        node.fields().forEachRemaining(entry -> settings.put(entry.getKey(), entry.getValue()));
    }

    /**
     * @see ConfigHandler#save()
     */
    @Override
    public void save() throws IOException {
        checkAndCreateFile();
        mapper.writeValue(configFile, settings);
    }

    /**
     * @see ConfigHandler#startAutoSave()
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
