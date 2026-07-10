package net.aerh.slashcommands.internal.handlers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModalErrorResponseTest {

    @Test
    @DisplayName("Should edit the original reply when the modal interaction is already acknowledged (e.g. deferred)")
    void editsOriginalReplyWhenAlreadyAcknowledged() {
        RecordingInteraction interaction = new RecordingInteraction(true);

        ModalInteractionHandler.respondWithGenericError(interaction.replyCallback());

        assertEquals(
                List.of("editOriginal(An error occurred while handling the modal submission.)", "queue"),
                interaction.calls());
    }

    @Test
    @DisplayName("Should send an ephemeral reply when the modal interaction is not yet acknowledged")
    void sendsEphemeralReplyWhenNotAcknowledged() {
        RecordingInteraction interaction = new RecordingInteraction(false);

        ModalInteractionHandler.respondWithGenericError(interaction.replyCallback());

        assertEquals(
                List.of("reply(An error occurred while handling the modal submission.)", "setEphemeral(true)", "queue"),
                interaction.calls());
    }
}
