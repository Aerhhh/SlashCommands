package net.aerh.slashcommands.internal.handlers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ComponentErrorResponseTest {

    @Test
    @DisplayName("Should send an ephemeral followup when the component interaction is already acknowledged (e.g. deferred edit)")
    void sendsEphemeralFollowupWhenAlreadyAcknowledged() {
        RecordingInteraction interaction = new RecordingInteraction(true);

        ComponentInteractionHandler.respondWithGenericError(interaction.replyCallback());

        assertEquals(
                List.of("sendMessage(An error occurred while handling the interaction.)", "setEphemeral(true)", "queue"),
                interaction.calls());
    }

    @Test
    @DisplayName("Should send an ephemeral reply when the component interaction is not yet acknowledged")
    void sendsEphemeralReplyWhenNotAcknowledged() {
        RecordingInteraction interaction = new RecordingInteraction(false);

        ComponentInteractionHandler.respondWithGenericError(interaction.replyCallback());

        assertEquals(
                List.of("reply(An error occurred while handling the interaction.)", "setEphemeral(true)", "queue"),
                interaction.calls());
    }
}
