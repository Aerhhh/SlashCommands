package net.aerh.slashcommands.internal.handlers;

/**
 * Base interface for all interaction handlers.
 * Provides common functionality for handling Discord interactions.
 */
public interface InteractionHandler {

    /**
     * Gets the type name of this handler for logging purposes.
     *
     * @return the handler type name
     */
    String getHandlerType();
}