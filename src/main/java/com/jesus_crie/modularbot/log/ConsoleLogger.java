package com.jesus_crie.modularbot.log;

/**
 * Used to log every log to the console.
 */
public class ConsoleLogger implements LogListener {

    /**
     * @see LogListener#onLog(Log)
     */
    @Override
    public void onLog(Log log) {
        System.out.println(log.toString());
    }

    /**
     * @see LogListener#onError(Log)
     */
    @Override
    public void onError(Log log) {
        System.err.println(log.toString());
    }
}
