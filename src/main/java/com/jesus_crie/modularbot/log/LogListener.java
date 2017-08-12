package com.jesus_crie.modularbot.log;

public interface LogListener {

    /**
     * Called when something is logged and it's not an error.
     * @param log the {@link Log}.
     */
    void onLog(Log log);

    /**
     * Called when an error is logged.
     * @param log the {@link Log}.
     */
    void onError(Log log);
}
