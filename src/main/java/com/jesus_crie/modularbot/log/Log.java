package com.jesus_crie.modularbot.log;

import com.jesus_crie.modularbot.utils.MiscUtils;
import net.dv8tion.jda.core.utils.Checks;
import org.slf4j.event.Level;

import static com.jesus_crie.modularbot.utils.F.f;

/**
 * Represent a log.
 */
public class Log {

    /**
     * The severity of the log.
     */
    public final Level LEVEL;

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
    public Log(Level level, String prefix, String message, Object content) {
        Checks.notNull(level, "level");
        Checks.notEmpty(message, "message");

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
