package com.jesus_crie.modularbot.manager;

import com.jesus_crie.modularbot.stats.Stats;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.hooks.InterfacedEventManager;

public class ModularEventManager extends InterfacedEventManager {

    @Override
    public void handle(Event event) {
        if (Stats.isEnable())
            Stats.incrementJDAEvent();
        //TODO rename thread.
        super.handle(event);
    }
}
