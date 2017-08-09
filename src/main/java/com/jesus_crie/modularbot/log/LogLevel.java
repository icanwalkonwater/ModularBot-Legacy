package com.jesus_crie.modularbot.log;

public enum LogLevel {
    DEBUG, INFO, WARNING, ERROR, FATAL;

    @Override
    public String toString() {
        return name().charAt(0) + name().toLowerCase().substring(1);
    }
}
