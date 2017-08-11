package com.jesus_crie.modularbot.log;

import com.jesus_crie.modularbot.utils.MiscUtils;

public interface Logger {

    /**
     * Handle anything that need to be logged.
     * @param level the severity of the log.
     * @param source a String representing the source of the log, for example "Command"
     * @param message the reason of this log.
     * @param content (Optionnal) bonus object that will be stringify.
     */
    void handle(LogLevel level, String source, String message, Object content);

    default void handle(LogLevel level, String from, String message) {
        handle(level, from, message, null);
    }

    void registerListener(LogListener... listeners);

    void unregisterListener(LogListener... listeners);

    default void debug(String from, String message) {
        handle(LogLevel.DEBUG, from, message);
    }

    default void info(String from, String message) {
        handle(LogLevel.INFO, from, message);
    }

    default void warning(String from, String message) {
        handle(LogLevel.WARNING, from, message);
    }

    default void fatal(String from, String message) {
        handle(LogLevel.FATAL, from, message);
    }

    /**
     * Handle an error.
     * @param e the exception.
     */
    default void error(String from, Throwable e) {
        handle(LogLevel.ERROR, from, MiscUtils.collectStackTrace(e));
    }
}
