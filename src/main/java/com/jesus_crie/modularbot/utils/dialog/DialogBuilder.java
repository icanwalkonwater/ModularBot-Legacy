package com.jesus_crie.modularbot.utils.dialog;

import com.jesus_crie.modularbot.sharding.ModularShard;
import com.jesus_crie.modularbot.template.EmbedTemplate;
import com.jesus_crie.modularbot.template.ModularTemplate;
import com.jesus_crie.modularbot.utils.Icons;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;

public class DialogBuilder {

    protected static final EmbedTemplate DEFAULT_TEMPLATE = new EmbedTemplate(new EmbedBuilder()
            .setColor(Color.WHITE)
            .setAuthor("Confirmation", null, Icons.QUESTION_MARK)
            .setDescription("{0}")
            .build());
    protected static final String ACCEPT = "\u2705";
    protected static final String DENY = "\u274E";

    private final ModularShard shard;
    protected MessageChannel channelTarget;
    protected User userTarget;
    private ModularTemplate template;
    private String[] emotes;

    public DialogBuilder(ModularShard shard) {
        this.shard = shard;
    }

    public DialogBuilder setTemplate(ModularTemplate template) {
        this.template = template;
        return this;
    }

    public DialogBuilder targetChannel(MessageChannel channel) {
        channelTarget = channel;
        return this;
    }

    public DialogBuilder targetUser(User user) {
        userTarget = user;
        return this;
    }

    public DialogBuilder useCustomEmote(String accept, String deny) {
        emotes = new String[] {accept, deny};
        return this;
    }

    public Boolean retrieveBlocking(Object... format) {
        //TODO
        return false;
    }
}
