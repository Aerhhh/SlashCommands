package net.aerh.slashcommands.internal.handlers;

import net.aerh.slashcommands.internal.core.SlashCommandRegistry;
import net.aerh.slashcommands.internal.execution.resolvers.ComponentArgumentResolver;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for Discord component interactions (buttons, select menus).
 * Manages component interaction execution and error handling.
 */
public class ComponentInteractionHandler implements InteractionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ComponentInteractionHandler.class);
    private static final String GENERIC_ERROR_MESSAGE = "An error occurred while handling the interaction.";

    private final SlashCommandRegistry registry;
    private final ComponentArgumentResolver argumentResolver;

    public ComponentInteractionHandler(SlashCommandRegistry registry) {
        this.registry = registry;
        this.argumentResolver = new ComponentArgumentResolver();
    }

    /**
     * Handles a button interaction event.
     *
     * @param event the button interaction event
     */
    public void handleButtonInteraction(ButtonInteractionEvent event) {
        String componentId = event.getComponentId();
        SlashCommandRegistry.ComponentInfo componentInfo = registry.getComponentHandler(componentId);

        if (componentInfo == null) {
            return;
        }

        try {
            executeComponentInteraction(componentInfo, event, componentId);
        } catch (Exception e) {
            handleComponentError(event, componentId, e);
        }
    }

    /**
     * Handles a string select interaction event.
     *
     * @param event the string select interaction event
     */
    public void handleStringSelectInteraction(StringSelectInteractionEvent event) {
        String componentId = event.getComponentId();
        SlashCommandRegistry.ComponentInfo componentInfo = registry.getComponentHandler(componentId);

        if (componentInfo == null) {
            return;
        }

        try {
            executeComponentInteraction(componentInfo, event, componentId);
        } catch (Exception e) {
            handleComponentError(event, componentId, e);
        }
    }

    private void executeComponentInteraction(SlashCommandRegistry.ComponentInfo componentInfo,
                                             Object event, String componentId) throws Exception {
        Object[] args = argumentResolver.prepareArguments(componentInfo.method(), event);
        componentInfo.method().invoke(componentInfo.instance(), args);
    }

    private void handleComponentError(GenericComponentInteractionCreateEvent event, String componentId, Exception e) {
        logger.error("Error handling component interaction '{}': {}", componentId, e.getMessage(), e);

        try {
            respondWithGenericError(event);
        } catch (Exception replyException) {
            logger.error("Failed to send error reply for component '{}': {}", componentId, replyException.getMessage());
        }
    }

    /**
     * Sends a generic error message to the user for a failed component interaction.
     * <p>
     * Component interactions are typically acknowledged via {@code deferEdit}, where editing the
     * original response would overwrite the message the component is attached to. If the
     * interaction was already acknowledged, an ephemeral followup message is sent instead of
     * editing; otherwise an ephemeral reply is sent.
     *
     * @param event the interaction to respond to
     */
    static void respondWithGenericError(IReplyCallback event) {
        InteractionErrorResponder.replyOrFollowUp(event, GENERIC_ERROR_MESSAGE);
    }

    @Override
    public String getHandlerType() {
        return "ComponentInteraction";
    }
}