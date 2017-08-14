package com.jesus_crie.modularbot.config;

import net.dv8tion.jda.core.entities.Guild;

import java.io.IOException;

public interface ConfigHandler {

    /**
     * Get a {@link Version} that represent the version of the application.
     * @return a version object.
     */
    Version getVersion();

    /**
     * Get the prefix for commands in a specific guild.
     * @param g can be null, if null return the global prefix.
     * @return a String representing the global command prefix or the
     *      guild specific prefix.
     */
    String getPrefixForGuild(Guild g);

    /**
     * Get the name of the application.
     * @return the given appName.
     */
    String getAppName();

    /**
     * Used to get your discord user id.
     * Used for command access level.
     * @return your user id.
     */
    long getCreatorId();

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
