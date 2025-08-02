package net.aerh.slashcommands.internal.execution.resolvers;

import java.lang.reflect.Method;

/**
 * Interface for resolving method arguments from Discord interaction events.
 * Implementations handle different types of interactions (slash commands, autocomplete, etc.).
 *
 * @param <T> the type of interaction event this resolver handles
 */
public interface ArgumentResolver<T> {

    /**
     * Prepares method arguments for invocation from the given interaction event.
     *
     * @param method the method to prepare arguments for
     * @param event  the interaction event
     * @return array of arguments ready for method invocation
     */
    Object[] prepareArguments(Method method, T event);
}