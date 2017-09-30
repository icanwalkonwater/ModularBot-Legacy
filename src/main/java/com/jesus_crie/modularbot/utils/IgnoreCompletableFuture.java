package com.jesus_crie.modularbot.utils;

import java.util.concurrent.CompletableFuture;

public abstract class IgnoreCompletableFuture<T> extends CompletableFuture<T> {

    /**
     * Same as {@link CompletableFuture#get()} but with exception handling.
     * Used when you don't really care if the future is fine or not (void futures for example).
     * @return an instance of {@link T} in case of success otherwise {@code null}.
     */
    @Override
    public T get() {
        try {
            return super.get();
        } catch (Exception ignore) {
            return null;
        }
    }
}
