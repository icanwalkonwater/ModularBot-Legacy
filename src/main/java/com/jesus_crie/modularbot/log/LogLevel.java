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

    public int getSeverity() {
        return severity;
    }

    public boolean isError() {
        return isError;
    }

    @Override
    public String toString() {
        return MiscUtils.capitalize(name());
    }
}
