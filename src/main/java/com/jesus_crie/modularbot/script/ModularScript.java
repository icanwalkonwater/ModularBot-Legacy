package com.jesus_crie.modularbot.script;

import com.jesus_crie.modularbot.exception.WrongScriptLocationException;
import jdk.nashorn.api.scripting.NashornScriptEngine;

import javax.script.ScriptEngineManager;
import java.io.File;

public class ModularScript {

    private final String name;
    private final File location;
    private final NashornScriptEngine engine;

    ModularScript(String name, File location, NashornScriptEngine engine) {
        this.name = name == null ? location.getName() : name;
        this.location = location;
        this.engine = engine;
    }

    public static final class ModularScriptBuilder {

        private String name;
        private String filename;
        private File location;
        private ScriptEngineManager manager;

        public ModularScriptBuilder(File location) {
            this.location = location;
        }

        public ModularScriptBuilder(String filename) {
            this(new File(filename));
        }

        public ModularScriptBuilder asGlobalScript() {
            location = new File("./scripts/" + filename);
            return this;
        }

        public ModularScriptBuilder asDecoratorScript() {
            //TODO
            return this;
        }

        public ModularScriptBuilder fromScriptManager(ScriptEngineManager manager) {
            this.manager = manager;
            return this;
        }

        public ModularScript build() {
            if (!location.exists() || !location.isFile() || !location.canRead() || !location.canWrite())
                throw new WrongScriptLocationException("Invalid location: " + location.getAbsolutePath());

            return new ModularScript(name, location, (NashornScriptEngine) manager.getEngineByName("nashorn"));
        }
    }
}
