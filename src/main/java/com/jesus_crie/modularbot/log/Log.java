package com.jesus_crie.modularbot.log;

import com.jesus_crie.modularbot.utils.MiscUtils;

import java.util.Objects;

import static com.jesus_crie.modularbot.utils.F.f;

/**
 * Represent a log.
 */
public class Log {

    /**
     * The severity of the log.
     */
    public final LogLevel LEVEL;

    /**
     * The name of the source thread.
     */
    public final String THREAD_NAME;

    /**
     * A prefix representing the source of the log, for example "Music"
     * May be null.
     */
    public final String PREFIX;

    /**
     * The message of the log.
     */
    public final String MESSAGE;

    /**
     * An optional object associated with the log.
     * May be null.
     */
    public final Object CONTENT;

    /**
     * Package-Private
     * Create an instance representing a specific log.
     * @param level the severity of the log.
     * @param prefix (optional) refer to the source of the log.
     * @param message the message associated with the log.
     * @param content (optional) an additional object that will be stringify with #toString().
     */
    public Log(LogLevel level, String prefix, String message, Object content) {
        Objects.requireNonNull(level, "level must not be null");
        Objects.requireNonNull(message, "message must not be null");

        LEVEL = level;
        PREFIX = prefix;
        THREAD_NAME = Thread.currentThread().getName();
        MESSAGE = message;
        CONTENT = content;
    }

    /**
     * Used to print the log.
     * If you want to change the way that logs are written, override this method.
     * @return a String representing the log.
     */
    @Override
    public String toString() {
        String format = "[%1$s] [%2$s] [%3$s] " + (PREFIX == null ? "" : "[%4$s] ") + "%5$s" + (CONTENT == null ? "" : ": %6$s");
        return f(format, MiscUtils.properTimestamp(System.currentTimeMillis(), "{hour}:{minutes}:{seconds}"), LEVEL, THREAD_NAME, PREFIX, MESSAGE, CONTENT);
    }
}
