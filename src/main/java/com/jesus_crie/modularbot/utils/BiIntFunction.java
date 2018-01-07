package com.jesus_crie.modularbot.utils;

/**
 * An {@link java.util.function.IntFunction} but with 2 ints.
 *
 * @param <T> The return type.
 */
@FunctionalInterface
public interface BiIntFunction<T> {

    T apply(int a, int b);
}
