package com.jesus_crie.modularbot.log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DefaultLogger implements Logger {

    protected final List<LogListener> listeners = new ArrayList<>();

    /**
     * @see Logger#handle(LogLevel, String, String, Object)
     */
    @Override
    public void handle(LogLevel level, String source, String message, Object content) {
        Log log = new Log(level, source, message, content);

        if (level.isError())
            listeners.forEach(l -> l.onError(log));
        else
            listeners.forEach(l -> l.onLog(log));
    }

    /**
     * @see Logger#registerListener(LogListener...)
     */
    @Override
    public void registerListener(LogListener... listeners) {
        Collections.addAll(this.listeners, listeners);
    }

    /**
     * @see Logger#unregisterListener(LogListener...)
     */
    @Override
    public void unregisterListener(LogListener... listeners) {
        this.listeners.removeAll(Arrays.asList(listeners));
    }
}
