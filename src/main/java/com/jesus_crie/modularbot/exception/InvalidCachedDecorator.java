package com.jesus_crie.modularbot.exception;

import com.jesus_crie.modularbot.messagedecorator.ReactionDecorator;

public class InvalidCachedDecorator extends RuntimeException {

    public InvalidCachedDecorator(ReactionDecorator decorator, String message) {
        super(decorator.getClass().getSimpleName() + "[" + decorator.getMessage().getIdLong() + "] " + message);
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
