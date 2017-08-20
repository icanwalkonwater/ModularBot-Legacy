package com.jesus_crie.modularbot.listener;

import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.exception.*;
import com.jesus_crie.modularbot.template.Templates;
import net.dv8tion.jda.core.exceptions.PermissionException;

import static com.jesus_crie.modularbot.utils.F.f;

public class DefaultCommandHandler implements CommandHandler {

    @Override
    public void onCommand(CommandEvent event) throws WrongContextException, LowAccessLevelException, MissingPermissionException, CommandFailedException, NoPatternException {
        // Check context.
        if (!event.getCommand().checkContext(event.getChannelType()))
            throw new WrongContextException(event);

        // Check Access Level.
        if (!event.getCommand().getAccessLevel().check(event.getTriggerEvent().getAuthor(), event.getTriggerEvent().getGuild()))
            throw new LowAccessLevelException(event);

        // Some logs.
        ModularBot.logger().info("Command", event.toString());

        // Start the matching of patterns.
        event.getCommand().execute(event);
    }

    @Override
    public void onCommandSuccess(CommandEvent event) {} // Ignore

    @Override
    public void onCommandNotFound(CommandNotFoundException e, String notFound) {} // Ignore

    @Override
    public void onCommandWrongContext(WrongContextException e) {
        e.getEvent().getChannel().sendMessage(Templates.ERROR_SIGNED(e.getEvent().getAuthor(), "This command isn't allowed here !").build()).queue();
    }

    @Override
    public void onCommandLowAccessLevel(LowAccessLevelException e) {
        e.getEvent().getChannel().sendMessage(Templates.ERROR_SIGNED(e.getEvent().getAuthor(), "You don't have the permission to use this command !").build()).queue();
    }

    @Override
    public void onCommandMissingPermission(MissingPermissionException e) {
        try {
            e.getEvent().getChannel().sendMessage(Templates.ERROR_SIGNED(e.getEvent().getAuthor(),
                    f("Sorry i need the permission \"%s\" to execute this command !", e.getMissingPermission().getName())).build()).queue();
        } catch (PermissionException e2) {
            e.getEvent().getAuthor().openPrivateChannel().complete()
                    .sendMessage(Templates.ERROR_SIGNED(e.getEvent().getAuthor(), "Sorry i don't have the permission to write messages.").build()).queue();
        }
    }

    @Override
    public void onCommandFailed(CommandFailedException e) {
        e.getEvent().getChannel().sendMessage(Templates.ERROR_SIGNED(e.getEvent().getAuthor(), "Something wrong happened...").build()).queue();
        ModularBot.logger().error("Command", e);
    }

    @Override
    public void onCommandNoPattern(NoPatternException e) {
        e.getEvent().getChannel().sendMessage(Templates.ERROR_SIGNED(e.getEvent().getAuthor(), "Sorry, the command you just tipped is invalid !").build()).queue();
    }

    @Override
    public void onCommandErrorUnknown(CommandException e) {
        e.getEvent().getChannel().sendMessage(Templates.ERROR.format(f("An unknown error happened: **%s** !\nPlease contact an administrator !", e.getMessage())).build()).queue();
    }
}
