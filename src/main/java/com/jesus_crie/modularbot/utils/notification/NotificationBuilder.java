package com.jesus_crie.modularbot.utils.notification;

import com.jesus_crie.modularbot.sharding.ModularShard;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class NotificationBuilder {

    protected static final String emoteDismiss = "\u274C";

    private String customDismiss;
    private User specifyTarget;
    private MessageChannel channel;
    private boolean dismissible = false;
    private boolean autoDismiss = false;
    private long timeout = 0;

    /**
     * Initialize a builder.
     * @param channel the channel were the notification will be displayed.
     */
    public NotificationBuilder(MessageChannel channel) {
        this.channel = channel;
    }

    /**
     * Used to override the default emote.
     * Can't be a guild emote.
     * @param emote the unicode form of the emote.
     * @return the current builder.
     */
    public NotificationBuilder useCustomEmote(String emote) {
        customDismiss = emote;
        return this;
    }

    /**
     * Used to specify who can remove this notification (if dismissible).
     * If not set, everyone who have access to the channel can dismiss the notification.
     * @param user the user that will be allow to dismiss the notification.
     * @return the current builder.
     */
    public NotificationBuilder singleTarget(User user) {
        specifyTarget = user;
        return this;
    }

    /**
     * Mark the notification as dismissible through an emote.
     * If the timeout is reached the notification can't be delete anymore by the user
     * but you still can use {@link ModularNotification#remove()}.
     * @return the current builder.
     */
    public NotificationBuilder dismissible() {
        dismissible = true;
        return this;
    }

    /**
     * The notification will be removed after the timeout.
     * @return the current builder.
     */
    public NotificationBuilder autoDismiss() {
        autoDismiss = true;
        return this;
    }

    /**
     * Use to set a timeout in milliseconds.
     * When the timeout is reached, this will trigger the auto dismiss
     * and the notification will not be dismissible anymore by the user.
     * @param timeout the timeout in milliseconds.
     * @return the current builder.
     */
    public NotificationBuilder useTimeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * Create the notification.
     * @return a new instance of {@link ModularNotification}.
     */
    public ModularNotification build() {
        return new ModularNotification((ModularShard) channel.getJDA(),
                channel,
                specifyTarget,
                customDismiss == null ? emoteDismiss : customDismiss,
                dismissible,
                autoDismiss,
                timeout);
    }
}
