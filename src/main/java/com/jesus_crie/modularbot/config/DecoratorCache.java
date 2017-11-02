package com.jesus_crie.modularbot.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.messagedecorator.ReactionDecorator;
import com.jesus_crie.modularbot.messagedecorator.dismissible.DialogDecorator;
import com.jesus_crie.modularbot.messagedecorator.dismissible.DismissibleDecorator;
import com.jesus_crie.modularbot.messagedecorator.dismissible.NotificationDecorator;
import com.jesus_crie.modularbot.messagedecorator.persistant.PersistantDecorator;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.CopyOnWriteArrayList;

// TODO
public class DecoratorCache {

    private final boolean allowDismissible;
    private final CopyOnWriteArrayList<ReactionDecorator> decorators = new CopyOnWriteArrayList<>();

    private final File file;
    private final ObjectMapper mapper;

    public DecoratorCache(boolean saveDismissible) {
        allowDismissible = saveDismissible;
        file = new File("./cache.json");

        mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(DialogDecorator.class, new DialogDecorator.DialogSerializer());
        module.addSerializer(NotificationDecorator.class, new NotificationDecorator.NotificationSerializer());
        mapper.registerModule(module);

        try {
            checkAndCreateFile();
        } catch (IOException e) {
            ModularBot.logger().fatal("Decorator Cache", "Failed to initialize cache file !");
            ModularBot.logger().error("Decorator Cache", e);
        }
    }

    private void checkAndCreateFile() throws IOException {
        if (!file.exists()) {
            ModularBot.logger().warning("Decorator Cache", "No cache file ! Creating one...");
            FileUtils.write(file, "{}", Charset.forName("UTF-8"));
            return;
        }

        BufferedReader reader = new BufferedReader(new FileReader(file));
        if (reader.readLine() == null) { // If file empty
            FileUtils.write(file, "{}", Charset.forName("UTF-8"));
        }
    }

    public void cacheDecorator(ReactionDecorator decorator) {
        if (decorator instanceof DismissibleDecorator && !allowDismissible) return;
        decorators.add(decorator);
    }

    public void saveCache() {
        try {
            mapper.writeValue(file, decorators);
        } catch (IOException e) {
            ModularBot.logger().fatal("Decorator Cache", "Failed to save cache !");
            ModularBot.logger().error("Decorator Cache", e);
        }
    }

    private static class PersistantSerializer extends StdSerializer<PersistantDecorator> {

        protected PersistantSerializer(Class<PersistantDecorator> t) {
            super(t);
        }

        @Override
        public void serialize(PersistantDecorator value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            gen.writeBooleanField("persistent", true);
            gen.writeNumberField("message", value.getMessage().getIdLong());
            gen.writeEndObject();
        }
    }

    private static class TestDeserializer extends StdDeserializer<PersistantDecorator> {

        protected TestDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public PersistantDecorator deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            return null;
        }
    }
}
