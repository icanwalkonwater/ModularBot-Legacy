package com.jesus_crie.modularbot.log;

import com.jesus_crie.modularbot.ModularBot;
import net.dv8tion.jda.core.utils.SimpleLog;
import org.slf4j.event.Level;

public class JDALogger implements SimpleLog.LogListener {

    @Override
    public void onLog(SimpleLog log, Level logLevel, Object message) {
        ModularBot.logger().handle(logLevel, log.getName(), message.toString());
    }

    @Override
    public void onError(SimpleLog log, Throwable err) {
        ModularBot.logger().error(log.getName(), err);
    }
}
