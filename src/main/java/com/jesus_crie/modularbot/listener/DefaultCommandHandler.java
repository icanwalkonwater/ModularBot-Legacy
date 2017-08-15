package com.jesus_crie.modularbot.listener;

import com.jesus_crie.modularbot.exception.*;
import com.jesus_crie.modularbot.template.Templates;
import net.dv8tion.jda.core.exceptions.PermissionException;

import static com.jesus_crie.modularbot.utils.F.f;

public class DefaultCommandHandler implements CommandHandler {

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
