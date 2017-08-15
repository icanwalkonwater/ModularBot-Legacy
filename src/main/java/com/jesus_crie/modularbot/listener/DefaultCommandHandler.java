package com.jesus_crie.modularbot.listener;

import com.jesus_crie.modularbot.exception.*;
import net.dv8tion.jda.core.exceptions.PermissionException;

import static com.jesus_crie.modularbot.utils.F.f;

public class DefaultCommandHandler implements CommandHandler {

    @Override
    public void onCommandWrongContext(WrongContextException e) {
        e.getTriggerEvent().getChannel().sendMessage(":no_entry_sign: Command not allowed here !").queue();
    }

    @Override
    public void onCommandLowAccessLevel(LowAccessLevelException e) {
        e.getTriggerEvent().getChannel().sendMessage(":no_entry_sign: You don't have the permission to use this command !").queue();
    }

    @Override
    public void onCommandMissingPermission(MissingPermissionException e) {
        try {
            e.getTriggerEvent().getChannel().sendMessage(f(":no_entry_sign: Sorry i need the permission %s to execute this command !",
                    e.getMissingPermission().getName())).queue();
        } catch (PermissionException e2) {
            e.getTriggerEvent().getAuthor().openPrivateChannel().complete()
                    .sendMessage(f(":no_entry_sign: Sorry, i need the permissions %s and %s to execute the command \"%s\" !",
                            e.getMissingPermission().getName(),
                            e2.getPermission().getName(),
                            e.getCommand().getName())).queue();
        }
    }

    @Override
    public void onCommandFailed(CommandFailedException e) {
        e.getTriggerEvent().getChannel().sendMessage(":no_entry_sign: Something wrong happened...").queue();
    }

    @Override
    public void onCommandNoPattern(NoPatternException e) {
        e.getTriggerEvent().getChannel().sendMessage(":no_entry_sign: Sorry, the command you just tipped is invalid !").queue();
    }

    @Override
    public void onCommandErrorUnknown(CommandException e) {
        e.getTriggerEvent().getChannel().sendMessage(":no_entry_sign: An unknown error happened ! Please contact an administrator !").queue();
    }
}
