package com.jesus_crie.modularbot.config;

import java.io.IOException;

public interface ConfigHandler {

    /**
     * Get a {@link com.jesus_crie.modularbot.config.Version Version} that represent the version of the application.
     * @return the version object.
     */
    Version getVersion();

    /**
     * Get the global prefix for commands.
     * @return a String representing the command prefix.
     */
    String getPrefix();

    /**
     * Used to save the config.
     * @throws IOException if there's a problem.
     */
    void load() throws IOException;

    /**
     * Used to load the config.
     * @throws IOException if there's a problem.
     */
    void save() throws IOException;
}
