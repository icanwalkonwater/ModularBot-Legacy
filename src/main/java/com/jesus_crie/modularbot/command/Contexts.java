package com.jesus_crie.modularbot.command;

import net.dv8tion.jda.core.entities.ChannelType;

import java.util.EnumSet;

public abstract class Contexts {

    public static final EnumSet<ChannelType> EVERYWHERE = EnumSet.allOf(ChannelType.class);
    public static final EnumSet<ChannelType> GUILD = EnumSet.of(ChannelType.TEXT);
    public static final EnumSet<ChannelType> GROUP = EnumSet.of(ChannelType.GROUP);
    public static final EnumSet<ChannelType> PRIVATE = EnumSet.of(ChannelType.PRIVATE);
    public static final EnumSet<ChannelType> NOT_GUILD = EnumSet.of(ChannelType.GROUP, ChannelType.PRIVATE);
}
