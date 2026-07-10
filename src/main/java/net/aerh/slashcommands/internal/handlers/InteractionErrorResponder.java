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

    /**
     * Replies ephemerally, or sends an ephemeral followup message when the interaction was
     * already acknowledged.
     * <p>
     * Suitable for interactions that are usually acknowledged via {@code deferEdit} (buttons,
     * select menus), where editing the original response would overwrite the message the
     * component is attached to.
     *
     * @param event   the interaction to respond to
     * @param message the error message to show the user
     */
    static void replyOrFollowUp(IReplyCallback event, String message) {
        if (event.isAcknowledged()) {
            event.getHook().sendMessage(message).setEphemeral(true).queue();
        } else {
            event.reply(message).setEphemeral(true).queue();
        }
    }
}
