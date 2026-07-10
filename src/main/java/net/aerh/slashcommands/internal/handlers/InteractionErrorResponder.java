package net.aerh.slashcommands.internal.handlers;

import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

/**
 * Shared error responses for interactions whose handler threw an exception.
 * <p>
 * Every strategy guarantees the user gets feedback even when the interaction was already
 * acknowledged (for example via {@code deferReply} or {@code deferEdit}), so the user is never
 * left on a permanent "thinking" state.
 */
final class InteractionErrorResponder {

    private InteractionErrorResponder() {
    }

    /**
     * Replies ephemerally, or edits the original response when the interaction was already
     * acknowledged.
     * <p>
     * Suitable for interactions where the acknowledged response is a reply owned by the bot
     * (slash commands, modal submissions), so overwriting it with the error message is safe.
     *
     * @param event   the interaction to respond to
     * @param message the error message to show the user
     */
    static void replyOrEditOriginal(IReplyCallback event, String message) {
        if (event.isAcknowledged()) {
            event.getHook().editOriginal(message).queue();
        } else {
            event.reply(message).setEphemeral(true).queue();
        }
    }
}
