package com.jesus_crie.modularbot.messagedecorator;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * If an object implement this interface, it can be cached.
 */
public interface Cacheable {

    void serialize(JsonGenerator gen, SerializerProvider provider) throws IOException;
}
