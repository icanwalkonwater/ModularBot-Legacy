package com.jesus_crie.modularbot.log;

import com.jesus_crie.modularbot.ModularBot;
import net.dv8tion.jda.core.utils.SimpleLog;

public class JDALogger implements SimpleLog.LogListener {

    @Override
    public void onLog(SimpleLog log, SimpleLog.Level logLevel, Object message) {
        ModularBot.logger().handle(LogLevel.fromJDALevel(logLevel), log.name, message.toString());
    }

    @Override
    public void onError(SimpleLog log, Throwable err) {
        ModularBot.logger().error(log.name, err);
    }
}
