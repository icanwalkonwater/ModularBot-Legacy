package com.jesus_crie.modularbot.template;

import java.text.MessageFormat;

/**
 * @param <T> the builder type.
 * @param <V> the output type.
 */
public abstract class ModularTemplate<T, V> {

    protected final T formatter;

    protected ModularTemplate(T formatter) {
        this.formatter = formatter;
    }

    /**
     * Format all the string that contains the formatter {@link T} and build it.
     * The formatter use {@link java.text.MessageFormat#format(String, Object...)} for the strings.
     * To use it you need to add "{index}" when you want to print an argument.
     * For example if you want the first, second and third argument: "First: {0}, Second: {1}, Third: {2}"
     * @param args some object to replace in the builder.
     * @return the output type {@link V}.
     */
    public abstract V format(Object... args);

    /**
     * Shortcut.
     * @see MessageFormat#format(String, Object...)
     */
    protected String f(String src, Object... args) {
        return MessageFormat.format(src, args);
    }
}
