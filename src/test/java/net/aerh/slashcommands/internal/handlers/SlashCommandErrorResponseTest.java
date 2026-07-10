package net.aerh.slashcommands.internal.handlers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SlashCommandErrorResponseTest {

    @Test
    @DisplayName("Should edit the original reply when the interaction is already acknowledged (e.g. deferred)")
    void editsOriginalReplyWhenAlreadyAcknowledged() {
        RecordingInteraction interaction = new RecordingInteraction(true);

        SlashCommandHandler.respondWithGenericError(interaction.replyCallback());

        assertEquals(
                List.of("editOriginal(An error occurred while executing the command.)", "queue"),
                interaction.calls());
    }

    @Test
    @DisplayName("Should send an ephemeral reply when the interaction is not yet acknowledged")
    void sendsEphemeralReplyWhenNotAcknowledged() {
        RecordingInteraction interaction = new RecordingInteraction(false);

        SlashCommandHandler.respondWithGenericError(interaction.replyCallback());

        assertEquals(
                List.of("reply(An error occurred while executing the command.)", "setEphemeral(true)", "queue"),
                interaction.calls());
    }
}
