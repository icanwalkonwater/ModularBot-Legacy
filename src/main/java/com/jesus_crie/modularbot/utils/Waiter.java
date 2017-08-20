package com.jesus_crie.modularbot.utils;

import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.listener.CommandEvent;
import com.jesus_crie.modularbot.listener.WaiterListener;
import com.jesus_crie.modularbot.sharding.ModularShard;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.Checks;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Waiter {

    /**
     * Used to detect incoming events on this shard.
     * Can be used to wait the next message of an user or a reaction on a specific message.
     * When an event <code>T</code> is triggered, <code>checker</code> is called to test the event and say if yes or no
     * the event is valid (check message id, author, ...).
     * If the event is valid, <code>onSuccess</code> is called.
     * If <code>singleTrigger</code> is true, onSuccess will not be called again and onTimeout will not be called either.
     * If <code>singleTrigger</code> is false, onSuccess can be called multiple times and onTimeout will always be triggered.
     * This method can't detect command events.
     * @param shard the current shard of the bot, used to register the listener.
     * @param event the type of event that you want to detect.
     * @param checker a predicate that will be used to check for example the author of the event.
     * @param onSuccess will be run if the event is triggered and the checker has returned true.
     * @param onTimeout will be run if the timeout is reached.
     * @param timeout the amount of time in milliseconds before the method times out.
     * @param singleTrigger if true, onSuccess will only be ran one or zero times and onTimeout may never be ran.
     * @param <T> the type of event that you want to get.
     */
    public static <T extends Event> void awaitEvent(final ModularShard shard, final Class<T> event,
                                                    final Predicate<T> checker, final Consumer<T> onSuccess, final Runnable onTimeout,
                                                    final long timeout, final boolean singleTrigger) {
        createListener(shard, event, checker, onSuccess, onTimeout, timeout, singleTrigger);
    }

    /**
     * Get the next event of type <code>T</code> on this shard.
     * Can be used to get the next event of an user for example.
     * This method is blocking until the right event is triggered.
     * If the method time out, this method will return null.
     * @param shard the current shard.
     * @param event the type of event that you want to get.
     * @param checker will be used as a middleware to check if this event is the right.
     * @param timeout the amount of milliseconds before the method times out.
     * @param <T> the type of event that you want to get.
     * @return the first event <code>T</code> that have passed the checker or null if the method has time out.
     */
    public static <T extends Event> T getNextEvent(final ModularShard shard, final Class<T> event,
                                                   final Predicate<T> checker, final long timeout) {
        final WaiterListener<T> future = createListener(shard, event, checker, null, null, timeout, true);

        try {
            return future.get();
        } catch (CancellationException e) {
            return null;
        } catch (Exception e) {
            ModularBot.logger().error("Waiter", e);
            return null;
        }
    }

    /**
     * Get the next message of an user on the current shard.
     * To get the next message in the same channel check {@link #getNextMessageFromUserInChannel(ModularShard, User, MessageChannel, long)}.
     * @param shard the current shard.
     * @param from the desired author of the message.
     * @param timeout the amount of time in milliseconds before the method times out.
     * @return the event corresponding to the next message sent by the user or null if the method has timed out.
     */
    public static MessageReceivedEvent getNextMessageFromUser(final ModularShard shard, final User from, final long timeout) {
        Checks.notNull(from, "user");

        return getNextEvent(shard,
                MessageReceivedEvent.class,
                e -> e.getAuthor().equals(from),
                timeout);
    }

    /**
     * Get the next message of a user in a specific channel.
     * @param shard the current shard.
     * @param from the desired author of the message.
     * @param channel the event must come from this channel.
     * @param timeout the amout of time in milliseconds before the method times out.
     * @return the next message of the user in the desired channel or null if the method has timed out.
     */
    public static MessageReceivedEvent getNextMessageFromUserInChannel(final ModularShard shard, final User from, final MessageChannel channel, final long timeout) {
        Checks.notNull(from, "user");
        Checks.notNull(channel, "channel");

        return getNextEvent(shard,
                MessageReceivedEvent.class,
                e -> e.getAuthor().equals(from) && e.getChannel().equals(channel),
                timeout);
    }

    /**
     * Used as a base to create the logic of all other methods of this class.
     */
    private static <T extends Event> WaiterListener<T> createListener(final ModularShard shard, final Class<T> event,
                                                     final Predicate<T> checker, final Consumer<T> onSuccess, final Runnable onTimeout,
                                                     final long timeout, final boolean singleTrigger) {
        // I hate NullPointerExceptions.
        Checks.notNull(shard, "shard");
        Checks.notNull(event, "event");

        // Can't detect command event.
        if (event.getName().equals(CommandEvent.class.getName()))
            throw new UnsupportedOperationException("You can't use CommandEvent here !");

        // Tasks.
        final WaiterListener<T> listener = new WaiterListener<>(shard, event);
        final ScheduledFuture timeoutFuture;

        // Set timeout task (if needed).
        if (timeout > 0)
            timeoutFuture = shard.getGeneralPool().schedule(() -> {
                if (onTimeout != null)
                    onTimeout.run();
                listener.cancel(true);
            }, timeout, TimeUnit.MILLISECONDS);
        else
            timeoutFuture = null;

        // Set listener.
        listener.setOnTrigger(e -> {
            if (checker == null)
                return true;

            if (checker.test(e)) {
                if (onSuccess != null)
                    onSuccess.accept(e);

                if (singleTrigger) {
                    if (timeoutFuture != null)
                        timeoutFuture.cancel(true);
                    return true;
                }
            }
            return false;
        });

        return listener;
    }
}
