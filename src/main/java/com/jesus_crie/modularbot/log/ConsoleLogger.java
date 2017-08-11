package com.jesus_crie.modularbot.log;

/**
 * Used to log everything to the console in a proper way.
 */
public class ConsoleLogger implements LogListener {

    @Override
    public void onLog(Log log) {
        System.out.println(log.toString());
    }

    @Override
    public void onError(Log log) {
        System.err.println(log.toString());
    }
}
