package com.jesus_crie.modularbot.template;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.util.Objects;

public class EmbedTemplate extends ModularTemplate<MessageEmbed, EmbedBuilder> {

    public EmbedTemplate(MessageEmbed embed) {
        super(embed);
    }

    @Override
    public EmbedBuilder format(final Object... args) {
        final EmbedBuilder builder = new EmbedBuilder();

        // Title and URL. Format
        if (Objects.nonNull(formatter.getTitle()))
            if (Objects.nonNull(formatter.getUrl()))
                builder.setTitle(f(formatter.getTitle(), args), formatter.getUrl());
            else
                builder.setTitle(f(formatter.getTitle(), args));
        // Description. Format
        if (Objects.nonNull(formatter.getDescription()))
            builder.setDescription(f(formatter.getDescription(), args));
        // Timestamp. No format
        if (Objects.nonNull(formatter.getTimestamp()))
            builder.setTimestamp(formatter.getTimestamp());
        // Color. No format
        if (Objects.nonNull(formatter.getColor()))
            builder.setColor(formatter.getColor());
        // Thumbnail. No format
        if (Objects.nonNull(formatter.getThumbnail()))
            builder.setThumbnail(formatter.getThumbnail().getUrl());
        // Author. Format
        if (Objects.nonNull(formatter.getAuthor()))
            builder.setAuthor(f(formatter.getAuthor().getName(), args),
                    Objects.nonNull(formatter.getAuthor().getUrl()) ? formatter.getAuthor().getUrl() : null,
                    Objects.nonNull(formatter.getAuthor().getIconUrl()) ? formatter.getAuthor().getIconUrl() : null);
        // Footer. Format partially
        if (Objects.nonNull(formatter.getFooter()))
            builder.setFooter(f(formatter.getFooter().getText(), args),
                    Objects.nonNull(formatter.getFooter().getIconUrl()) ? formatter.getFooter().getIconUrl() : null);
        // Image. No format
        if (Objects.nonNull(formatter.getImage()))
            builder.setImage(formatter.getImage().getUrl());
        // Fields. Format
        if (!formatter.getFields().isEmpty())
            formatter.getFields().forEach(f -> builder.addField(
                    Objects.nonNull(f.getName()) ? f(f.getName(), args) : null,
                    Objects.nonNull(f.getValue()) ? f(f.getValue(), args) : null,
                    f.isInline()));

        return builder;
    }
}
