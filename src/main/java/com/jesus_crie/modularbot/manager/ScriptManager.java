package com.jesus_crie.modularbot.manager;

import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.script.ModularScript;

import javax.script.ScriptEngineManager;
import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

public class ScriptManager {

    private final ScriptEngineManager engine;
    private final ConcurrentHashMap<String, File> scriptsLocation;
    private final ConcurrentHashMap<String, ModularScript> loadedScript;

    public ScriptManager() {
        engine = new ScriptEngineManager();
        scriptsLocation = new ConcurrentHashMap<>();
        loadedScript = new ConcurrentHashMap<>();

        engine.put("modular", ModularBot.instance());
    }

    public void registerScript(String name, File file) {
        scriptsLocation.put(name, file);
    }
}
