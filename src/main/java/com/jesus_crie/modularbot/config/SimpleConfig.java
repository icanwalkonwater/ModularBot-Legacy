package com.jesus_crie.modularbot.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * A simple config wrapper that load config from a file called "config.json" using an {@link com.fasterxml.jackson.databind.ObjectMapper ObjectMapper}.
 * It can store any object but for complex object it's better to use the Jackson Databind serialization system with {@link com.fasterxml.jackson.databind.ser.std.StdSerializer StdSerializer}.
 */
public class SimpleConfig implements ConfigHandler {

    private static final ObjectMapper mapper = new ObjectMapper();

    private final File configFile;
    private final Version version;
    private final HashMap<String, JsonNode> settings = new HashMap<>();

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public SimpleConfig(String configFile, Version version) throws IOException {
        this.version = version;
        this.configFile = new File(configFile);
        if (!this.configFile.exists())
            this.configFile.createNewFile();
        load();
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public String getPrefix() {
        return "/";
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
     * @return the value as a {@link com.fasterxml.jackson.databind.JsonNode JsonNode} or null if not found.
     */
    public JsonNode getSettingAsJsonObject(String name) {
        return settings.get(name);
    }

    @Override
    public void load() throws IOException {
        JsonNode node = mapper.readValue(configFile, JsonNode.class);
        node.fields().forEachRemaining(entry -> settings.put(entry.getKey(), entry.getValue()));
    }

    @Override
    public void save() throws IOException {
        mapper.writeValue(configFile, settings);
    }
}
