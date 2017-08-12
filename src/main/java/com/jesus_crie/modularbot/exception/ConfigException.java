package com.jesus_crie.modularbot.exception;

/**
 * An exception that can be throw in config actions like loading and saving.
 */
public class ConfigException extends Exception {

    public ConfigException(String message) {
        super(message);
    }
}
