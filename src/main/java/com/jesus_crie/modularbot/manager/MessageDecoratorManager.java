package com.jesus_crie.modularbot.manager;

import com.jesus_crie.modularbot.config.DecoratorCache;
import com.jesus_crie.modularbot.exception.AlreadyExistingDecorator;
import com.jesus_crie.modularbot.messagedecorator.ReactionDecorator;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Manage the active decorator.
 */
public final class MessageDecoratorManager {

    private final ConcurrentHashMap<Long, ReactionDecorator> decorators = new ConcurrentHashMap<>();
    private final DecoratorCache cache;

    public MessageDecoratorManager(boolean saveDismissible) {
        cache = new DecoratorCache(saveDismissible);
    }

    /**
     * Get the decorator cache associated.
     * @return the associated {@link DecoratorCache}.
     */
    public DecoratorCache getCache() {
        return cache;
    }

    /**
     * Used to register a decorator, automatically done when creating one.
     * @param decorator the decorator to register.
     * @throws AlreadyExistingDecorator if a decorator is already bind to the message.
     */
    public void registerDecorator(ReactionDecorator decorator) throws AlreadyExistingDecorator {
        if (!decorators.containsKey(decorator.getMessage().getIdLong())) decorators.put(decorator.getMessage().getIdLong(), decorator);
        else throw new AlreadyExistingDecorator("A decorator is already bind to message " + decorator.getMessage().getIdLong());
    }

    /**
     * Unregister a decorator from the manager, automatically done in {@link ReactionDecorator#destroy(boolean)}.
     * @param decorator the decorator to unregister.
     */
    public void unregister(ReactionDecorator decorator) {
        decorators.remove(decorator.getMessage().getIdLong());
        cache.uncacheDecorator(decorator);
    }

    /**
     * Get the decorator corresponding to the given message.
     * @param messageId the id of the message.
     * @return the corresponding decorator.
     */
    public ReactionDecorator getDecoratorForMessage(long messageId) {
        return decorators.getOrDefault(messageId, null);
    }

    /**
     * Get how many decorators are active.
     * @return the number of active decorator.
     */
    public int size() {
        return decorators.size();
    }

    /**
     * Destroy all decorators in a blocking way.
     */
    public void destroyAll() {
        decorators.forEach((id, dec) -> dec.destroy(false));
    }

    /**
     * Destroy all decorators each in a different thread of the given thread pool.
     * Used to save time when too many decorators are present.
     * @param pool the pool to use.
     */
    public void destroyAllAsync(ScheduledExecutorService pool, int threadCount) {
        final int decoratorPerThread = (int) Math.floor(((float) size()) / ((float) threadCount)); // how many decorators can fit in a thread.
        final Collection<ReactionDecorator> values = Collections.unmodifiableCollection(decorators.values()); // a copy of the values to avoid concurrence.

        for (int i = 0; i < threadCount; i++) {
            final int toSkip = decoratorPerThread * i;
            pool.execute(() -> values.stream()
                    .skip(toSkip)
                    .limit(decoratorPerThread)
                    .forEach(d -> d.destroy(false)));
        }

        // if not every decorator has been destroyed
        if (values.stream().skip(decoratorPerThread * threadCount).count() > 0)
            values.stream()
                .skip(decoratorPerThread * threadCount)
                .forEach(d -> d.destroy(false));
    }
}
