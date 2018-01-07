package com.jesus_crie.modularbot.messagedecorator.dismissible;

import com.jesus_crie.modularbot.messagedecorator.ReactionButton;
import com.jesus_crie.modularbot.messagedecorator.ReactionDecoratorBuilder;
import com.jesus_crie.modularbot.sharding.ModularShard;
import com.jesus_crie.modularbot.utils.IgnoreCompletableFuture;
import com.jesus_crie.modularbot.utils.Waiter;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.utils.Checks;

import java.util.function.Consumer;

public class DialogDecorator extends DismissibleDecorator {

    /**
     * The button used to confirm.
     * The emote used is "✅".
     */
    protected static final ReactionButton ACCEPT_BUTTON = new ReactionButton("\u2705", (event, decorator) -> ((DialogDecorator) decorator).onTrigger(true));
    /**
     * The button used to deny.
     * The emote used is "❎".
     */
    protected static final ReactionButton DENY_BUTTON = new ReactionButton("\u274E", (event, decorator) -> ((DialogDecorator) decorator).onTrigger(false));

    protected final CompletableFuture completable;
    protected Consumer<Boolean> callback;

    /**
     * Main constructor
     * See {@link DialogBuilder} for more details.
     */
    protected DialogDecorator(Message bind, User target, long timeout) {
        super(bind, target, timeout, false, ACCEPT_BUTTON, DENY_BUTTON);
        completable = new CompletableFuture();

        listener = Waiter.createListener(((ModularShard) bind.getJDA()), MessageReactionAddEvent.class,
                e -> isAlive && e.getMessageIdLong() == bind.getIdLong() && e.getUser().equals(target),
                this::click, () -> onTrigger(null),
                timeout, true);

        if (super.callback != null) super.callback.onReady(this);
    }

    /**
     * Triggered when one of the 2 buttons are pressed or when the timeout is reached.
     * @param res the result, true/false if the dialog has been triggered otherwise null.
     */
    protected void onTrigger(Boolean res) {
        if (res == null) destroy(true);
        else dismiss();
        completable.complete(res);
        if (callback != null) callback.accept(res);
    }

    /**
     * Add a callback method to this dialog box.
     * Can be used to retrieve the value asynchronously.
     * @param callback the callback.
     */
    public void setCallback(Consumer<Boolean> callback) {
        this.callback = callback;
    }

    /**
     * Get the result of the dialog in a blocking way.
     * @return the answer of the user or null if the timeout has been reached.
     */
    public Boolean get() {
        return completable.get();
    }

    /**
     * An {@link IgnoreCompletableFuture} for booleans.
     */
    private static class CompletableFuture extends IgnoreCompletableFuture<Boolean> {}

    /**
     * The builder for this decorator.
     */
    public static final class DialogBuilder extends ReactionDecoratorBuilder.DecoratorTargetBuilder<DialogBuilder, DialogDecorator> {

        private long timeout = 60000L;

        /**
         * (Recommended) If you use a timeout, the dialog will automatically return if the user is not responding.
         * If the timeout is reached, the dialog will return null.
         * By default, 1 minute.
         * Use 0 for infinite (not recommended).
         * @param timeout the timeout in milliseconds.
         * @return the current builder.
         */
        @Override
        public DialogBuilder useTimeout(long timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * Create a new {@link DialogDecorator} based on this builder.
         * @param bind the targeted message.
         * @param target the targeted user.
         * @return a new instance of {@link DialogDecorator}.
         */
        @Override
        public DialogDecorator bindAndBuild(Message bind, User target) {
            Checks.notNull(bind, "message");
            Checks.notNull(target, "target");
            return new DialogDecorator(bind, target, timeout);
        }
    }
}
