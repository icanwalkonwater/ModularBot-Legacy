package com.jesus_crie.modularbot.template;

import com.jesus_crie.modularbot.utils.Icons;
import com.jesus_crie.modularbot.utils.MiscUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;

public class Templates {

    public static final ModularTemplate<MessageEmbed, EmbedBuilder> ERROR = new EmbedTemplate(new EmbedBuilder()
            .setColor(Color.RED)
            .setAuthor("An error occured", null, Icons.ERROR)
            .setDescription("{0}")
    );

    public static final ModularTemplate<MessageEmbed, EmbedBuilder> GLOBAL = new EmbedTemplate(new EmbedBuilder()
            .setColor(Color.WHITE)
            .setAuthor("{0}", null, Icons.INFORMATION)
    );

    public static EmbedBuilder ERROR_SIGNED(User author, Object... args) {
        return ERROR.format(args).setFooter("Requested by " + MiscUtils.stringifyUser(author), author.getEffectiveAvatarUrl());
    }

    public static EmbedBuilder GLOBAL_SIGNED(User author, Object... args) {
        return GLOBAL.format(args).setFooter("Requested by " + MiscUtils.stringifyUser(author), author.getEffectiveAvatarUrl());
    }
}
