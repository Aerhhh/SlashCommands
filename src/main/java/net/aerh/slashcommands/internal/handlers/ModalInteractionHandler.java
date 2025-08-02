package net.aerh.slashcommands.internal.handlers;

import net.aerh.slashcommands.internal.core.SlashCommandRegistry;
import net.aerh.slashcommands.internal.execution.resolvers.ModalArgumentResolver;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for Discord modal interactions.
 * Manages modal interaction execution and error handling.
 */
public class ModalInteractionHandler implements InteractionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ModalInteractionHandler.class);

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

        if (!event.isAcknowledged()) {
            event.reply("An error occurred while handling the modal submission.").setEphemeral(true).queue();
        }
    }

    @Override
    public String getHandlerType() {
        return "ModalInteraction";
    }
}