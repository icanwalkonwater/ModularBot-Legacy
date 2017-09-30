package com.jesus_crie.modularbot.utils.dialog;

import com.jesus_crie.modularbot.sharding.ModularShard;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.Checks;

import java.util.concurrent.ExecutionException;

/**
 * Used to easily manipulate a {@link ModularDialog}.
 */
public class DialogBuilder {

    protected static final String ACCEPT = "\u2705";
    protected static final String DENY = "\u274E";

    protected User userTarget;
    private String[] emotes;
    private boolean deleteAfter = false;
    private long timeout = 0;

    /**
     * Used the user that can react to this dialog box.
     * @param user the user to target.
     * @return the current builder.
     */
    public DialogBuilder targetUser(User user) {
        Checks.notNull(user, "user");
        userTarget = user;
        return this;
    }

    /**
     * Use if you want to use other unicode emotes.
     * @param accept the emote to confirm.
     * @param deny the emote to cancel the dialog.
     * @return the current builder.
     */
    public DialogBuilder useCustomEmote(String accept, String deny) {
        Checks.noWhitespace(accept, "accept");
        Checks.noWhitespace(deny, "deny");
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

    /**
     * Bind the given message to a new instance of {@link ModularDialog}.
     * @param message the message to attach.
     * @return the current builder.
     */
    public ModularDialog bind(Message message) {
        Checks.notNull(message, "message");
        return new ModularDialog(((ModularShard) message.getJDA()), message, userTarget,
                emotes == null ? ACCEPT : emotes[0], emotes == null ? DENY : emotes[1], timeout, deleteAfter);
    }

    /**
     * Same as {@link #bind(Message)} but return the result in a blocking way.
     * @param message the message to bind.
     * @return the possibly-null result of the {@link ModularDialog}.
     */
    public Boolean bindAndRetrieve(Message message) throws ExecutionException, InterruptedException {
        return bind(message).get();
    }

    /**
     * Same as {@link #bindAndRetrieve(Message)} but with a default value in case of timeout or error.
     * @param message the message to bind.
     * @param defaultValue the default value used if an error occurred or when the {@link ModularDialog} timed out.
     * @return true if the user has clicked yes, false if he had clicked no, otherwise return {@code defaultValue}.
     */
    public boolean bindAndRetrieveOrDefault(Message message, boolean defaultValue) {
        try {
            Boolean result = bindAndRetrieve(message);
            return result == Boolean.TRUE || result != Boolean.FALSE && defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
