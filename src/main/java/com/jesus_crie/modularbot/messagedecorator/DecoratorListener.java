package com.jesus_crie.modularbot.messagedecorator;

import com.jesus_crie.modularbot.messagedecorator.dismissible.DismissibleDecorator;
import com.jesus_crie.modularbot.messagedecorator.persistant.PollDecorator;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.User;

/**
 * Can be attached to a {@link ReactionDecorator} and listen to events happening in.
 * These methods are called before the action is performed and can cancel them.
 */
public interface DecoratorListener {

    /**
     * When a button is clicked.
     * @param decorator the targeted decorator.
     * @param button the button that has been clicked.
     * @return true to stop the action.
     */
    default boolean onClick(ReactionDecorator decorator, ReactionButton button) { return true; }

    /**
     * When a vote is added or removed.
     * Only triggered on {@link com.jesus_crie.modularbot.messagedecorator.persistant.PollDecorator}.
     * @param decorator the targeted decorator.
     * @param emote the emote that correspond to the vote.
     * @param voter the voter.
     * @param isVote if the user is adding a voting or removing it
     * @return true to stop the action.
     */
    default boolean onVote(PollDecorator decorator, MessageReaction.ReactionEmote emote, User voter, boolean isVote) { return true; }

    /**
     * Called when a decorator is dismissed.
     * Only on {@link com.jesus_crie.modularbot.messagedecorator.dismissible.DismissibleDecorator}.
     * @param decorator the targeted decorator.
     * @return true to stop the action.
     */
    default boolean onDismiss(DismissibleDecorator decorator) { return true; }

    /**
     * Called when the decorator is destroyed.
     * This one can't cancel the action but is called before the decorator is destroyed.
     * @param decorator the targeted decorator.
     */
    default void onDestroy(ReactionDecorator decorator) {}
}
