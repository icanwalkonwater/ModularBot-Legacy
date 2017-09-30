package com.jesus_crie.modularbot.utils.menu;

import com.jesus_crie.modularbot.sharding.ModularShard;
import com.jesus_crie.modularbot.utils.IgnoreCompletableFuture;
import com.jesus_crie.modularbot.utils.Waiter;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.utils.Checks;

import java.util.HashMap;
import java.util.function.Consumer;

public class ModularMenu extends IgnoreCompletableFuture<Void> {

    private final Message target;
    private final User author;
    private final HashMap<String, MenuClickListener> listeners = new HashMap<>();
    private final long timeout;
    private final boolean deleteAfter;

    public ModularMenu(final Message target, final User author, long timeout, boolean deleteAfter) {
        this.target = target;
        this.author = author;
        this.timeout = timeout;
        this.deleteAfter = deleteAfter;
    }

    /**
     * Create a button below the menu and associate a listener with it.
     * @param emoteButton the reaction, can be plain unicode or the id of a guild emote.
     * @param listener the listener that will be triggered when the button is clicked.
     * @return the current menu.
     */
    public ModularMenu addButtonWithListener(String emoteButton, MenuClickListener listener) {
        Checks.notNull(emoteButton, "emote");
        Checks.notNull(listener, "listener");

        Emote gEmote = target.getGuild().getEmoteById(emoteButton);

        if (gEmote == null)
            target.addReaction(emoteButton).complete();
        else
            target.addReaction(gEmote).complete();
        listeners.put(emoteButton, listener);
        return this;
    }

    /**
     * Start the menu and close it when a button is triggered.
     * If no button are triggered the menu will be closed when the timeout is reached.
     * This method is asynchronous. If you want to make it blocking, call {@link #get()} just after this method.
     */
    public void run() {
        Waiter.awaitEvent((ModularShard) target.getJDA(),
                MessageReactionAddEvent.class,
                e -> listeners.keySet().contains(e.getReactionEmote().getName()) && e.getUser().equals(author),
                e -> {
                    MenuClickListener l = listeners.get(e.getReactionEmote().getName());
                    if (l != null)
                        l.accept(e);
                    close();
                },
                this::close,
                timeout,
                true);
    }

    /**
     * Finish the menu and delete the target message.
     */
    public void close() {
        if (deleteAfter) {
            try {
                target.delete().complete();
            } catch (Exception ignore) {}
        }
        complete(null);
    }

    /**
     * A shortcut for a {@code Consumer<MessageReactionAddEvent>}.
     */
    @FunctionalInterface
    public interface MenuClickListener extends Consumer<MessageReactionAddEvent> {}
}
