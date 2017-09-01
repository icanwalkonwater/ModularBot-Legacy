package com.jesus_crie.modularbot.manager;

import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.ModularBuilder;
import com.jesus_crie.modularbot.stats.Stats;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.InterfacedEventManager;

public class ModularEventManager extends InterfacedEventManager {

    /**
     * All JDA events will pass here.
     * Used to collect some stats (if {@link ModularBuilder#useStats()} was called.
     * JDA do the rest.
     * @param event the JDA event to be handled.
     */
    @Override
    public void handle(Event event) {
        if (!ModularBot.isReady() && !(event instanceof ReadyEvent))
            return;
        if (Stats.isEnable())
            Stats.incrementJDAEvent();
        super.handle(event);
    }
}
