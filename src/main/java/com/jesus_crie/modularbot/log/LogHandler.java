package com.jesus_crie.modularbot.log;

import com.jesus_crie.modularbot.utils.MiscUtils;
import org.slf4j.event.Level;

public interface LogHandler {

    /**
     * Handle anything that need to be logged.
     * @param level the severity of the log.
     * @param source a String representing the source of the log, for example "Command"
     * @param message the reason of this log.
     * @param content (Optional) bonus object that will be stringify.
     */
    void handle(Level level, String source, String message, Object content);

    /**
     * Overload of {@link LogHandler#handle(Level, String, String, Object)} with no object.
     */
    default void handle(Level level, String from, String message) {
        handle(level, from, message, null);
    }

    /**
     * Used to register some {@link LogListener} that will receive every log that is throw by Modular.
     * @param listeners some listeners that will receive the logs.
     */
    void registerListener(LogListener... listeners);

    /**
     * Used to remove some {@link LogListener}, they will no longer receive logs.
     * @param listeners the listeners to remove.
     */
    void unregisterListener(LogListener... listeners);

    /**
     * Overload of {@link LogHandler#handle(Level, String, String)}.
     * Log something with the {@link LogLevel#DEBUG} level.
     */
    default void debug(String from, String message) {
        handle(Level.DEBUG, from, message);
    }

    /**
     * Overload of {@link LogHandler#handle(Level, String, String)}.
     * Log something with the {@link LogLevel#INFO} level.
     */
    default void info(String from, String message) {
        handle(Level.INFO, from, message);
    }

    /**
     * Overload of {@link LogHandler#handle(Level, String, String)}.
     * Log something with the {@link LogLevel#WARNING} level.
     */
    default void warning(String from, String message) {
        handle(Level.WARN, from, message);
    }

    /**
     * Overload of {@link LogHandler#handle(Level, String, String)}.
     * Log something with the {@link LogLevel#FATAL} level.
     */
    default void fatal(String from, String message) {
        handle(Level.ERROR, from, message);
    }

    /**
     * Overload of {@link LogHandler#handle(Level, String, String)}
     * Used to log {@link Exception} and their stack trace.
     * @param from a String representing the source of the log, for example "Command"
     * @param e the exception to log.
     */
    default void error(String from, Throwable e) {
        handle(Level.ERROR, from, MiscUtils.collectStackTrace(e));
    }
}
