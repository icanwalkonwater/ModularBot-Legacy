package com.jesus_crie.modularbot.utils.dialog;

import com.jesus_crie.modularbot.sharding.ModularShard;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

/**
 * Used to easily manipulate a {@link ModularDialog}.
 */
public class DialogBuilder {

    protected static final String ACCEPT = "\u2705";
    protected static final String DENY = "\u274E";

    protected User userTarget;
    private String[] emotes;
    private Emote[] emoteGuild;
    private boolean deleteAfter = false;
    private long timeout = 0;

    /**
     * Used the user that can react to this dialog box.
     * @param user the user to target.
     * @return the current builder.
     */
    public DialogBuilder targetUser(User user) {
        userTarget = user;
        return this;
    }

    /**
     * Use if you want to use other unicode emotes.
     * If you want to use guild emotes, set this to null and add the emotes yourself.
     * @param accept the emote to confirm.
     * @param deny the emote to cancel the dialog.
     * @return the current builder.
     */
    public DialogBuilder useCustomEmote(String accept, String deny) {
        emotes = new String[] {accept, deny};
        return this;
    }

    /**
     * Use if you want the message to be automatically deleted after triggered.
     * @return the current builder.
     */
    public DialogBuilder deleteAfterTrigger() {
        deleteAfter = true;
        return this;
    }

    /**
     * Use if you want to set a timeout to your dialog.
     * Warning: if the dialog timed out, null will be returned and not false !
     * @param timeout the timeout.
     * @return the current builder.
     */
    public DialogBuilder useTimeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

    public ModularDialog bind(Message message) {
        return new ModularDialog(((ModularShard) message.getJDA()), message, userTarget,
                emotes == null ? ACCEPT : emotes[0], emotes == null ? DENY : emotes[1], timeout, deleteAfter);
    }

    public Boolean bindAndRetrieve(Message message) {
        return bind(message).get();
    }

    public boolean bindAndRetrieveOrDefault(Message message, boolean defaultValue) {
        Boolean result = bindAndRetrieve(message);
        return result == Boolean.TRUE || result != Boolean.FALSE && defaultValue;

    }
}
