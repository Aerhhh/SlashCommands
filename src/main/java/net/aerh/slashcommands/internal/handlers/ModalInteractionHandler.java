package net.aerh.slashcommands.internal.handlers;

import net.aerh.slashcommands.internal.core.SlashCommandRegistry;
import net.aerh.slashcommands.internal.execution.resolvers.ModalArgumentResolver;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for Discord modal interactions.
 * Manages modal interaction execution and error handling.
 */
public class ModalInteractionHandler implements InteractionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ModalInteractionHandler.class);
    private static final String GENERIC_ERROR_MESSAGE = "An error occurred while handling the modal submission.";

    private final SlashCommandRegistry registry;
    private final ModalArgumentResolver argumentResolver;

    public ModalInteractionHandler(SlashCommandRegistry registry) {
        this.registry = registry;
        this.argumentResolver = new ModalArgumentResolver();
    }

    /**
     * Handles a modal interaction event.
     *
     * @param event the modal interaction event
     */
    public void handleModalInteraction(ModalInteractionEvent event) {
        String modalId = event.getModalId();
        SlashCommandRegistry.ModalInfo modalInfo = registry.getModalHandler(modalId);

        if (modalInfo == null) {
            return;
        }

        try {
            executeModalInteraction(modalInfo, event);
        } catch (Exception e) {
            handleModalError(event, modalId, e);
        }
    }

    private void executeModalInteraction(SlashCommandRegistry.ModalInfo modalInfo, ModalInteractionEvent event) throws Exception {
        Object[] args = argumentResolver.prepareArguments(modalInfo.method(), event);
        modalInfo.method().invoke(modalInfo.instance(), args);
    }

    private void handleModalError(ModalInteractionEvent event, String modalId, Exception e) {
        logger.error("Error handling modal interaction '{}': {}", modalId, e.getMessage(), e);
        respondWithGenericError(event);
    }

    /**
     * Sends a generic error message to the user for a failed modal submission.
     * <p>
     * If the interaction was already acknowledged (for example via {@code deferReply}), the original
     * response is edited so the user is not left on a permanent "thinking" state. Otherwise an
     * ephemeral reply is sent.
     *
     * @param event the interaction to respond to
     */
    static void respondWithGenericError(IReplyCallback event) {
        InteractionErrorResponder.replyOrEditOriginal(event, GENERIC_ERROR_MESSAGE);
    }

    @Override
    public String getHandlerType() {
        return "ModalInteraction";
    }
}