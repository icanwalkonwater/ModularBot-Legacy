package com.jesus_crie.modularbot.log;

import com.jesus_crie.modularbot.utils.MiscUtils;

public interface LogHandler {

    /**
     * Handle every logs that are not an error.
     * @param log the description to log.
     * @param level the severity.
     */
    void handle(Object log, LogLevel level);

    default void debug(Object log) {
        handle(log, LogLevel.DEBUG);
    }

    default void info(Object log) {
        handle(log, LogLevel.INFO);
    }

    default void warning(Object log) {
        handle(log, LogLevel.WARNING);
    }

    default void fatal(Object log) {
        handle(log, LogLevel.FATAL);
    }

    /**
     * Handle an error.
     * @param e the exception.
     */
    default void error(Throwable e) {
        handle(e.getMessage(), LogLevel.ERROR);
        handle(MiscUtils.collectStackTrace(e), LogLevel.ERROR);
    }
}
