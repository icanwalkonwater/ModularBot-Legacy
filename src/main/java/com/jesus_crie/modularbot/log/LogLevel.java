package com.jesus_crie.modularbot.log;

import com.jesus_crie.modularbot.utils.MiscUtils;

public enum LogLevel {

    DEBUG(-1, false),
    INFO(0, false),
    WARNING(1, false),
    ERROR(2, true),
    FATAL(3, true);

    private final int severity;
    private final boolean isError;
    LogLevel(int s, boolean e) {
        severity = s;
        isError = e;
    }

    /**
     * Get the level or severity of the log as an <b>int</b>.
     * @return an <b>int</b> representing the severity of the log.
     * @see LogLevel#severity
     */
    public int getSeverity() {
        return severity;
    }

    /**
     * Check if the log is considered as an error to print it
     * in the right form.
     * @return true if this level is considered has an error.
     */
    public boolean isError() {
        return isError;
    }

    /**
     * Print the name of the level in lower case with the
     * first letter capitalized.
     * @return the name of the level with the first letter capitalized.
     */
    @Override
    public String toString() {
        return MiscUtils.capitalize(name());
    }
}
